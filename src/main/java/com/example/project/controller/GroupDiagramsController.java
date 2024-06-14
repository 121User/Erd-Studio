package com.example.project.controller;

import com.example.project.model.Dto.DiagramOutputDto;
import com.example.project.model.Dto.GroupOutputDto;
import com.example.project.model.Entity.Diagram;
import com.example.project.model.Entity.Group;
import com.example.project.model.Entity.User;
import com.example.project.service.DiagramService;
import com.example.project.service.GroupService;
import com.example.project.service.GroupUserService;
import com.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.project.util.ListProcessingUtil.*;
import static com.example.project.util.SessionUtil.getLongAttrFromSession;

@RestController
@RequestMapping("/group/{groupId}/diagram/list")
public class GroupDiagramsController {
    private final UserService userService;
    private final DiagramService diagramService;
    private final GroupService groupService;
    private final GroupUserService groupUserService;

    @Autowired
    public GroupDiagramsController(UserService userService, DiagramService diagramService,
                                   GroupService groupService, GroupUserService groupUserService) {
        this.userService = userService;
        this.diagramService = diagramService;
        this.groupService = groupService;
        this.groupUserService = groupUserService;
    }

    @RequestMapping("")
    public ModelAndView viewGroupDiagramListPage(@PathVariable(name = "groupId") Long groupId,
                                                 @RequestParam(name = "searchText", required = false) String searchText,
                                                 HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Group group = groupService.getByID(groupId);
            //Проверка наличия пользователя в группе
            if (groupUserService.checkUserInGroup(user, group)) {
                String userRole = groupUserService.getUserRoleInGroup(user, group);

                List<Diagram> diagramList = filterDiagramListByOwner(group.getDiagrams(), userId); //Фильтрация по владельцу
                diagramList = sortDiagramListByModDate(diagramList); //Отсортированный список диаграмм группы

                //Фильтрация списка диаграмм в зависимости от роли (если владелец или администратор, то выводятся все диаграммы, иначе только свои)
                if (group.getOwner().getId().equals(userId) || userRole.equals("admin")) {
                    List<Diagram> otherDiagramList = new ArrayList<>();
                    for (Diagram diagram : group.getDiagrams()) {
                        if (!diagramList.contains(diagram))
                            otherDiagramList.add(diagram);
                    }
                    diagramList.addAll(sortDiagramListByModDate(otherDiagramList));
                }
                //Создание объекта группы для вывода
                GroupOutputDto groupOutputDto = new GroupOutputDto(group.getId(), group.getName(),
                        group.getOwner().getName(), null, null, null);

                ModelAndView modelAndView = new ModelAndView("group_diagram_list_page");
                modelAndView.addObject("userName", user.getName());
                modelAndView.addObject("group", groupOutputDto);
                modelAndView.addObject("searchText", searchText);

                //Поиск по названию диаграммы
                if (searchText != null) {
                    diagramList = filterDiagramListBySearch(diagramList, searchText);
                }
                //Вывод сообщения, если список диаграмм пуст
                if (diagramList.isEmpty()) {
                    modelAndView.addObject("listInfo", "Список диаграмм пуст");
                } else {
                    //Получение обработанного списка диаграмм для вывода
                    List<DiagramOutputDto> diagramOutputDtoList = diagramService.getDiagramOutputDtoList(diagramList);
                    modelAndView.addObject("diagramList", diagramOutputDtoList);
                }
                return modelAndView;
            }
        }
        return new ModelAndView("redirect:/main");
    }

    @RequestMapping("/delete/{diagramId}")
    public ModelAndView deleteGroupDiagram(@PathVariable(name = "groupId") Long groupId,
                                           @PathVariable(name = "diagramId") Long diagramId,
                                           HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            diagramService.deleteDiagram(diagramId);
        } else {
            return new ModelAndView("redirect:/main");
        }
        return new ModelAndView("redirect:/group/" + groupId + "/diagram/list");
    }
}