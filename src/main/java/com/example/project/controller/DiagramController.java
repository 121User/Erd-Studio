package com.example.project.controller;

import com.example.project.model.Dto.DiagramHistoryOutputDto;
import com.example.project.model.Dto.DiagramOutputDto;
import com.example.project.model.Entity.Diagram;
import com.example.project.model.Entity.DiagramAccessLevel;
import com.example.project.model.Entity.Group;
import com.example.project.model.Entity.User;
import com.example.project.service.DiagramService;
import com.example.project.service.GroupService;
import com.example.project.service.GroupUserService;
import com.example.project.service.UserService;
import com.example.project.util.ImportDiagramUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

import static com.example.project.util.Helper.encryptUrl;
import static com.example.project.util.ListProcessingUtil.*;
import static com.example.project.util.SessionUtil.*;

@RestController
@RequestMapping("/diagram")
public class DiagramController {
    private final UserService userService;
    private final DiagramService diagramService;
    private final GroupUserService groupUserService;

    @Autowired
    public DiagramController(UserService userService, DiagramService diagramService, GroupUserService groupUserService) {
        this.userService = userService;
        this.diagramService = diagramService;
        this.groupUserService = groupUserService;
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
            List<Diagram> diagramList = sortDiagramListByModDate(userService.getPrivateDiagramList(user));
            if (searchText != null) {
                diagramList = filterDiagramListBySearch(sortDiagramListByModDate(diagramList), searchText);
            }
            //Вывод сообщения, если список диаграмм пуст
            if (diagramList.isEmpty()) {
                modelAndView.addObject("listInfo", "Список диаграмм пуст");
            } else {
                modelAndView.addObject("diagramList", diagramList);
            }
        } else {
            modelAndView = new ModelAndView("redirect:/main");
        }
        return modelAndView;
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
                                      HttpServletRequest request) {
        if (!fileDiagram.isEmpty()) {
            try {
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
                }
                return new ModelAndView("redirect:/main");
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
        }
        return new ModelAndView("redirect:/main");
    }

    @RequestMapping("/{diagramId}")
    public ModelAndView viewDiagramWorkingPage(@PathVariable(name = "diagramId") Long diagramId,
                                               HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        ModelAndView modelAndView = new ModelAndView("diagram_working_page");
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Optional<Diagram> diagramOpt = diagramService.getOptByID(diagramId);
            //Проверка существования диаграммы
            if (diagramOpt.isPresent()) {
                Diagram diagram = diagramOpt.get();
                //Создание объекта диаграммы для вывода
                DiagramOutputDto diagramOutputDto = new DiagramOutputDto(diagram.getId(), diagram.getName(),
                        diagram.getOwner().getName(), null, null,
                        diagram.getCode(), diagram.getDiagramAccessLevel().getName(),
                        encryptUrl(diagramService.getDiagramUrl(diagram.getId())));

                //Проверка, является ли пользователь владельцем диаграммы
                if (!diagram.getOwner().getId().equals(userId)) {
                    //Проверка правильности подключения
                    Long diagramIdToConnect = getLongAttrFromSession(request, "diagramIdToConnect");
                    Group group = diagram.getGroup();
                    if (diagramIdToConnect.equals(diagramId)) {
                        //Подключение по ссыклке на диаграмму
                        DiagramAccessLevel diagramAccessLevel = diagram.getDiagramAccessLevel();
                        if (diagramAccessLevel.getName().equals("access is closed")) {
                            //Проверка уровня доступа к группе
                            return new ModelAndView("redirect:/main?message=Diagram access is closed");
                        }
                    } else if (group != null && groupUserService.checkUserInGroup(user, group)) {
                        //Подключение из списка диаграмм группы
                        diagramOutputDto.setAccessLevel("read and write access");
                    } else {
                        return new ModelAndView("redirect:/main");
                    }
                }
                boolean theme = user.getDesignTheme().getName().equals("dark");
                List<DiagramHistoryOutputDto> diagramHistoryList = sortDiagramHistoryListByModDate(
                        diagramService.getDiagramHistoryOutputDtoList(diagram.getDiagramHistories()));

                modelAndView.addObject("userName", user.getName());
                modelAndView.addObject("designTheme", theme);
                modelAndView.addObject("diagram", diagramOutputDto);
                //Вывод сообщения, если список диаграмм пуст
                if (diagramHistoryList.isEmpty()) {
                    modelAndView.addObject("listInfo", "Список изменений пуст");
                } else {
                    modelAndView.addObject("diagramHistoryList", diagramHistoryList);
                }
                return modelAndView;
            }
            return new ModelAndView("redirect:/main?message=Diagram deleted");
        } else {
            //Создание объекта диаграммы для вывода
            DiagramOutputDto diagramOutputDto = new DiagramOutputDto((long) 0, "Новая диаграмма",
                    null, null, null, null, null, null);
            modelAndView.addObject("diagram", diagramOutputDto);
            modelAndView.addObject("designTheme", false);
            return modelAndView;
        }
    }

    @RequestMapping("/{diagramId}/save")
    public ModelAndView saveDiagramChanges(@PathVariable(name = "diagramId") Long diagramId,
                                           @RequestParam(name = "designTheme") String designTheme,
                                           @RequestParam(name = "diagramName") String diagramName,
                                           @RequestParam(name = "diagramCode") String diagramCode,
                                           HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Optional<Diagram> diagramOpt = diagramService.getOptByID(diagramId);
            //Проверка существования диаграммы
            if (diagramOpt.isPresent()) {
                Diagram diagram = diagramOpt.get();
                User owner = diagram.getOwner();
                List<Diagram> diagramList = userService.getDiagramListForUniqueName(owner, diagram.getGroup());
                diagramService.change(diagram, diagramName, diagramCode, diagramList, user);
                userService.changeDesignTheme(user, designTheme);
                return new ModelAndView("redirect:/diagram/" + diagramId);
            }
            return new ModelAndView("redirect:/main?message=Diagram deleted");
        }
        return new ModelAndView("redirect:/main");
    }

    @RequestMapping("/{diagramId}/rollback-history/{historyId}")
    public ModelAndView rollbackDiagramHistory(@PathVariable(name = "diagramId") Long diagramId,
                                               @PathVariable(name = "historyId") Long historyId,
                                               HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            Optional<Diagram> diagramOpt = diagramService.getOptByID(diagramId);
            //Проверка существования диаграммы
            if (diagramOpt.isPresent()) {
                diagramService.rollback(diagramOpt.get(), historyId);
                return new ModelAndView("redirect:/diagram/" + diagramId);
            }
            return new ModelAndView("redirect:/main?message=Diagram deleted");
        }
        return new ModelAndView("redirect:/main");
    }

    @RequestMapping("/{diagramId}/change-access")
    public ModelAndView changeDiagramAccessLevel(@PathVariable(name = "diagramId") Long diagramId,
                                                 @RequestParam(name = "level") Long accessLevel,
                                                 HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            Optional<Diagram> diagramOpt = diagramService.getOptByID(diagramId);
            //Проверка существования диаграммы
            if (diagramOpt.isPresent()) {
                Diagram diagram = diagramOpt.get();
                //Проверка на соответствие авторизованного пользователя владельцу диаграммы
                if (userId.equals(diagram.getOwner().getId())) {
                    diagramService.changeAccessLevel(diagram, accessLevel);
                }
                return new ModelAndView("redirect:/diagram/" + diagramId);
            }
            return new ModelAndView("redirect:/main?message=Diagram deleted");
        }
        return new ModelAndView("redirect:/group/" + diagramId + "/participant/list");
    }

    @RequestMapping("/list/delete/{diagramId}")
    public ModelAndView deleteDiagram(@PathVariable(name = "diagramId") Long diagramId,
                                      HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            diagramService.deleteDiagram(diagramId);
        }
        return new ModelAndView("redirect:/diagram/list");
    }

    @RequestMapping("/{diagramId}/connect")
    public ModelAndView connectToDiagram(@PathVariable(name = "diagramId") Long diagramId,
                                         HttpServletRequest request) {
        setAttrToSession(request, "diagramIdToConnect", diagramId);
        return new ModelAndView("redirect:/diagram/" + diagramId);
    }
}