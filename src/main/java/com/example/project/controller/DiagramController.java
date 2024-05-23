package com.example.project.controller;

import com.example.project.model.Entity.Diagram;
import com.example.project.model.Entity.User;
import com.example.project.service.DiagramService;
import com.example.project.service.UserService;
import com.example.project.util.ImportDiagramUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

import static com.example.project.util.ListProcessingUtil.filterDiagramListBySearch;
import static com.example.project.util.ListProcessingUtil.sortDiagramListByModDate;
import static com.example.project.util.SessionUtil.checkUserAuthorization;
import static com.example.project.util.SessionUtil.getLongAttrFromSession;

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
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        ModelAndView modelAndView;
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            modelAndView = new ModelAndView("diagram_list_page");
            modelAndView.addObject("userName", user.getName());
            modelAndView.addObject("searchText", searchText);

            //Поиск по названию диаграммы
            List<Diagram> diagramList = sortDiagramListByModDate(user.getDiagrams());
            if (searchText != null) {
                diagramList = filterDiagramListBySearch(sortDiagramListByModDate(user.getDiagrams()), searchText);
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
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            userService.deleteDiagram(userId, diagramId);
        }
        return new ModelAndView("redirect:/diagram/list");
    }

    @RequestMapping("/list/import")
    public ModelAndView viewDiagramImportPage(@RequestParam(name = "groupId", required = false) Long groupId,
                                              HttpServletRequest request) {
        if (checkUserAuthorization(request)) {
            Long userId = getLongAttrFromSession(request, "userId");
            String userName = userService.getNameById(userId);
            ModelAndView modelAndView = new ModelAndView("diagram_import_page");
            modelAndView.addObject("userName", userName);
            modelAndView.addObject("groupId", groupId);
            return modelAndView;
        }
        return new ModelAndView("redirect:/main");
    }

    @PostMapping("/list/import/do")
    public ModelAndView importDiagram(@RequestParam(name = "groupId") String groupIdOpt,
                                      @ModelAttribute(name = "fileDiagram") MultipartFile fileDiagram,
                                      HttpServletRequest request){
        if (!fileDiagram.isEmpty()) {
            try{
                //Импорт из файла
                StringBuilder contentFile = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileDiagram.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        contentFile.append(line).append("\n");
                    }
                }
                String diagramName = "Новая диаграмма";
                String diagramCode = ImportDiagramUtil.getDiagramCodeForImport(contentFile.toString());
                if (fileDiagram.getOriginalFilename() != null) {
                    diagramName = fileDiagram.getOriginalFilename().split("\\.")[0];
                }
                Long userId = getLongAttrFromSession(request, "userId");
                Optional<User> userOpt = userService.getById(userId);

                //Проверка существования пользователя в системе
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    Long diagramId = userService.addDiagram(user, groupIdOpt, diagramName, diagramCode);
                    return new ModelAndView("redirect:/diagram/" + diagramId);
                } else {
                    return new ModelAndView("redirect:/main");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ModelAndView("redirect:/diagram/list/import");
    }

    @RequestMapping("/list/create")
    public ModelAndView viewDiagramCreatePage(@RequestParam(name = "groupId", required = false) Long groupId,
                                              HttpServletRequest request) {
        if (checkUserAuthorization(request)) {
            Long userId = getLongAttrFromSession(request, "userId");
            String userName = userService.getNameById(userId);
            ModelAndView modelAndView = new ModelAndView("diagram_create_page");
            modelAndView.addObject("userName", userName);
            modelAndView.addObject("groupId", groupId);
            return modelAndView;
        }
        return new ModelAndView("redirect:/main");

    }

    @PostMapping("/list/create/do")
    public ModelAndView createDiagram(@RequestParam(name = "groupId") String groupIdOpt,
                                      @ModelAttribute(name = "name") String diagramName,
                                      HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Long diagramId = userService.addDiagram(user, groupIdOpt, diagramName, null);
            return new ModelAndView("redirect:/diagram/" + diagramId);
        } else {
            return new ModelAndView("redirect:/main");
        }
    }

    @RequestMapping("/{diagramId}")
    public ModelAndView viewDiagramWorkingPage(@PathVariable(name = "diagramId") Long diagramId,
                                               HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Diagram diagram = diagramService.getByID(diagramId);

            boolean theme = user.getDesignTheme().getName().equals("dark");
            ModelAndView modelAndView = new ModelAndView("diagram_working_page");
            modelAndView.addObject("userName", user.getName());
            modelAndView.addObject("diagramName", diagram.getName());
            modelAndView.addObject("diagramCode", diagram.getCode());
            modelAndView.addObject("designTheme", theme);
            return modelAndView;
        } else {
            ModelAndView modelAndView = new ModelAndView("diagram_working_page");
            modelAndView.addObject("diagramName", "Новая диаграмма");
            modelAndView.addObject("designTheme", false);
            return modelAndView;
        }
    }

    @RequestMapping("/{diagramId}/save")
    public ModelAndView saveChanges(@PathVariable(name = "diagramId") Long diagramId,
                                    @RequestParam(name = "designTheme") String designTheme,
                                    @RequestParam(name = "diagramName") String diagramName,
                                    @RequestParam(name = "diagramCode") String diagramCode,
                                    HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
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
}