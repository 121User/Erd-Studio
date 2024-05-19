package com.example.project.controller;

import com.example.project.model.Dto.DiagramDto;
import com.example.project.model.Entity.Diagram;
import com.example.project.model.Entity.User;
import com.example.project.service.DiagramService;
import com.example.project.service.UserService;
import com.example.project.util.ExportImportUtil;
import com.example.project.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.project.util.Helper.getFilteredDiagramList;
import static com.example.project.util.Helper.getSortedDiagramList;

@RestController
@RequestMapping("/diagram")
public class DiagramController {
    private final UserService userService;
    private final DiagramService diagramService;

    @Autowired
    public DiagramController(UserService userService, DiagramService diagramService) {
        this.userService = userService;
        this.diagramService = diagramService;
    }

    @RequestMapping("/list")
    public ModelAndView viewDiagramListPage(@RequestParam(name = "searchText", required = false) String searchText,
                                            HttpServletRequest request) {
        Long userId = SessionUtil.getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        ModelAndView modelAndView;
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            modelAndView = new ModelAndView("diagram_list_page");
            modelAndView.addObject("userEmail", user.getEmail());
            modelAndView.addObject("searchText", searchText);

            //Поиск по названию диаграммы
            List<Diagram> diagramList = getSortedDiagramList(user.getDiagrams());
            if (searchText != null) {
                diagramList = getFilteredDiagramList(getSortedDiagramList(user.getDiagrams()), searchText);
            }
            //Вывод сообщения, если список диаграмм пуст
            if (user.getDiagrams().isEmpty()) {
                modelAndView.addObject("listInfo", "Список диаграмм пуст");
            } else {
                modelAndView.addObject("diagramList", diagramList);
            }
        } else {
            modelAndView = new ModelAndView("redirect:/main");
        }
        return modelAndView;
    }

    @RequestMapping("/list/delete/{diagramId}")
    public ModelAndView deleteDiagram(@PathVariable(name = "diagramId") Long diagramId,
                                      HttpServletRequest request) {
        Long userId = SessionUtil.getLongAttrFromSession(request, "userId");
        userService.deleteDiagram(userId, diagramId);
        return new ModelAndView("redirect:/diagram/list");
    }

    @RequestMapping("/list/import")
    public ModelAndView viewDiagramImportPage(HttpServletRequest request) {
        if (SessionUtil.checkUserAuthorization(request)) {
            Long userId = SessionUtil.getLongAttrFromSession(request, "userId");
            String userEmail = userService.getEmailById(userId);
            ModelAndView modelAndView = new ModelAndView("diagram_import_page");
            modelAndView.addObject("userEmail", userEmail);
            return modelAndView;
        }
        return new ModelAndView("redirect:/main");
    }

    @RequestMapping("/list/import/do")
    public ModelAndView importDiagram(@ModelAttribute(name = "fileDiagram") MultipartFile fileDiagram,
                                      @ModelAttribute(name = "codeDiagram") String codeDiagram,
                                      HttpServletRequest request) throws IOException {
        if (!fileDiagram.isEmpty() || !codeDiagram.equals("")) {
            String code;
            String name = "Новая диаграмма";
            //Импорт из файла
            if (!fileDiagram.isEmpty()) {
                StringBuilder contentFile = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileDiagram.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        contentFile.append(line).append("\n");
                    }
                }
                code = ExportImportUtil.getDiagramCodeForImport(contentFile.toString());
                name = fileDiagram.getOriginalFilename();
            }
            //Импорт из введенного текста
            else {
                code = codeDiagram.replace("\\n", "");
//                code = ExportImportUtil.getDiagramCodeForImport(codeDiagram);
            }
            Long userId = SessionUtil.getLongAttrFromSession(request, "userId");
            Optional<User> userOpt = userService.getById(userId);
            //Проверка существования пользователя в системе
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                DiagramDto diagramDto = new DiagramDto(name, LocalDateTime.now(),
                        LocalDateTime.now(), code);
                Long diagramId = userService.addDiagram(user, diagramDto);
                return new ModelAndView("redirect:/diagram/" + diagramId);
            } else {
                return new ModelAndView("redirect:/main");
            }
        }
        return new ModelAndView("redirect:/diagram/list/import");
    }

    @RequestMapping("/list/create")
    public ModelAndView viewDiagramCreatePage(HttpServletRequest request) {
        if (SessionUtil.checkUserAuthorization(request)) {
            Long userId = SessionUtil.getLongAttrFromSession(request, "userId");
            String userEmail = userService.getEmailById(userId);
            ModelAndView modelAndView = new ModelAndView("diagram_create_page");
            modelAndView.addObject("userEmail", userEmail);
            return modelAndView;
        }
        return new ModelAndView("redirect:/main");

    }

    @RequestMapping("/list/create/do")
    public ModelAndView createDiagram(@ModelAttribute(name = "name") String diagramName,
                                      HttpServletRequest request) {
        Long userId = SessionUtil.getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            DiagramDto diagramDto = new DiagramDto(diagramName, LocalDateTime.now(), LocalDateTime.now(), null);
            Long diagramId = userService.addDiagram(user, diagramDto);
            return new ModelAndView("redirect:/diagram/" + diagramId);
        } else {
            return new ModelAndView("redirect:/main");
        }
    }

    @RequestMapping("/{diagramId}")
    public ModelAndView viewDiagramWorkingPage(@PathVariable(name = "diagramId") Long diagramId,
                                        HttpServletRequest request) {
        Long userId = SessionUtil.getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Diagram diagram = diagramService.getByID(diagramId);

            boolean theme = user.getDesignTheme().getName().equals("dark");
            ModelAndView modelAndView = new ModelAndView("diagram_working_page");
            modelAndView.addObject("userEmail", user.getEmail());
            modelAndView.addObject("diagramName", diagram.getName());
            modelAndView.addObject("diagramCode", diagram.getCode());
            modelAndView.addObject("designTheme", theme);
            return modelAndView;
        } else {
            return new ModelAndView("redirect:/main");
        }
    }

    @RequestMapping( "/{diagramId}/save")
    public ModelAndView saveChanges(@PathVariable(name = "diagramId") Long diagramId,
                                    @RequestParam(name = "designTheme") String designTheme,
                                    @RequestParam(name = "diagramName") String diagramName,
                                    @RequestParam(name = "diagramCode") String diagramCode,
                                    HttpServletRequest request) {
        Long userId = SessionUtil.getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Diagram diagram = diagramService.getByID(diagramId);
            diagramService.changeName(diagram, diagramName, user);
            diagramService.changeCode(diagram, diagramCode);
            userService.changeDesignTheme(user, designTheme);
            return new ModelAndView("redirect:/diagram/" + diagramId);
        } else {
            return new ModelAndView("redirect:/main");
        }
    }

    @RequestMapping("/{diagramId}/export")
    public ModelAndView exportCode(@PathVariable(name = "diagramId") Long diagramId,
                                   @RequestParam(name = "type") String type,
                                   HttpServletResponse response) throws IOException {
        response.setContentType("text/plain; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        Diagram diagram = diagramService.getByID(diagramId);

        String encodedFileName = URLEncoder.encode(diagram.getName() + ".sql", StandardCharsets.UTF_8.toString());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=" + encodedFileName;
        response.setHeader(headerKey, headerValue);

        String text = "";
        if (type.equals("mssql")) {
            text = ExportImportUtil.convertToMsSqlServer(diagram.getCode());
        } else if (type.equals("postgresql")) {
            text = ExportImportUtil.convertToPostgresql(diagram.getCode());
        }

        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(text.getBytes(StandardCharsets.UTF_8));
        outputStream.close();
        return new ModelAndView("redirect:/diagram/" + diagramId);
    }
}