package com.example.project.controller;

import com.example.project.model.Dto.GroupOutputDto;
import com.example.project.model.Entity.Group;
import com.example.project.model.Entity.GroupAccessLevel;
import com.example.project.model.Entity.GroupUser;
import com.example.project.model.Entity.User;
import com.example.project.service.GroupService;
import com.example.project.service.GroupUserService;
import com.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

import static com.example.project.util.ListProcessingUtil.filterGroupListBySearch;
import static com.example.project.util.ListProcessingUtil.sortGroupListByCreateDate;
import static com.example.project.util.SessionUtil.checkUserAuthorization;
import static com.example.project.util.SessionUtil.getLongAttrFromSession;

@RestController
@RequestMapping("/group")
public class GroupController {
    private final UserService userService;
    private final GroupService groupService;
    private final GroupUserService groupUserService;

    @Autowired
    public GroupController(UserService userService, GroupService groupService, GroupUserService groupUserService) {
        this.userService = userService;
        this.groupService = groupService;
        this.groupUserService = groupUserService;
    }

    @RequestMapping("/list")
    public ModelAndView viewGroupListPage(@RequestParam(name = "searchText", required = false) String searchText,
                                          HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        ModelAndView modelAndView;
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            modelAndView = new ModelAndView("group_list_page");
            modelAndView.addObject("userName", user.getName());
            modelAndView.addObject("searchText", searchText);

            //Поиск по названию группы
            List<Group> groupList = sortGroupListByCreateDate(user.getGroups()); //Добавление групп, созданных пользователем
            groupList.addAll(sortGroupListByCreateDate(groupUserService.getAllGroupsByUser(user))); //Добавление групп, в которые вошел пользователь

            if (searchText != null) {
                groupList = filterGroupListBySearch(groupList, searchText);
            }
            //Вывод сообщения, если список групп пуст
            if (groupList.isEmpty()) {
                modelAndView.addObject("listInfo", "Список групп пуст");
            } else {
                //Получение обработанного списка групп для вывода
                List<GroupOutputDto> groupOutputDtoList = groupService.getGroupOutputDtoList(groupList);
                modelAndView.addObject("groupList", groupOutputDtoList);
            }
        } else {
            modelAndView = new ModelAndView("redirect:/main");
        }
        return modelAndView;
    }

    @RequestMapping("/list/create")
    public ModelAndView viewGroupCreatePage(HttpServletRequest request) {
        if (checkUserAuthorization(request)) {
            Long userId = getLongAttrFromSession(request, "userId");
            String userName = userService.getNameById(userId);
            ModelAndView modelAndView = new ModelAndView("group_create_page");
            modelAndView.addObject("userName", userName);
            return modelAndView;
        }
        return new ModelAndView("redirect:/main");
    }

    @PostMapping("/list/create/do")
    public ModelAndView createGroup(@ModelAttribute(name = "name") String groupName,
                                    HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Long groupId = userService.addGroup(user, groupName);
            return new ModelAndView("redirect:/group/" + groupId + "/diagram/list");
        } else {
            return new ModelAndView("redirect:/main");
        }
    }

    @RequestMapping("/{groupId}/rename")
    public ModelAndView renameGroup(@PathVariable(name = "groupId") Long groupId,
                                    @RequestParam(name = "nameText") String groupName,
                                    HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Group group = groupService.getByID(groupId);
            //Проверка на соответствие авторизованного пользователя владельцу группы или администратору
            if (userId.equals(group.getOwner().getId())) {
                groupService.changeName(groupId, groupName, user);
            }
        }
        return new ModelAndView("redirect:" + request.getHeader("Referer"));
    }

    @RequestMapping("/{groupId}/change-access")
    public ModelAndView changeGroupAccessLevel(@PathVariable(name = "groupId") Long groupId,
                                               @RequestParam(name = "level") Long accessLevel,
                                               HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            Group group = groupService.getByID(groupId);
            //Проверка на соответствие авторизованного пользователя владельцу группы
            if (userId.equals(group.getOwner().getId())) {
                groupService.changeAccessLevel(group, accessLevel);
            }
        }
        return new ModelAndView("redirect:/group/" + groupId + "/participant/list");
    }

    @RequestMapping("/list/delete/{groupId}")
    public ModelAndView deleteGroup(@PathVariable(name = "groupId") Long groupId,
                                    HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Group group = groupService.getByID(groupId);
        //Проверка является ли отправитель запроса владельцем группы
        if (userId.equals(group.getOwner().getId())) {
            userService.deleteGroup(groupId);
        } else {
            groupUserService.leaveGroup(userId, groupId);
        }
        return new ModelAndView("redirect:/group/list");
    }

    @RequestMapping("/{groupId}/connect")
    public ModelAndView connectToGroup(@PathVariable(name = "groupId") Long groupId,
                                       HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Optional<Group> groupOpt = groupService.getOptByID(groupId);

            //Проверка существования группы
            if (groupOpt.isPresent()) {
                Group group = groupOpt.get();
                Optional<GroupUser> groupUserOpt = groupUserService.getByUserAndGroup(user, group);

                //Проверка на наличие пользователя в группе
                if (!group.getOwner().getId().equals(userId) && groupUserOpt.isEmpty()) {
                    GroupAccessLevel groupAccessLevel = group.getGroupAccessLevel();

                    //Проверка уровня доступа к группе
                    if (groupAccessLevel.getName().equals("entry access")) {
                        groupUserService.create(group, user);
                    } else {
                        return new ModelAndView("redirect:/main?message=Group access is closed");
                    }
                }
                return new ModelAndView("redirect:/group/" + groupId + "/diagram/list");
            } else {
                return new ModelAndView("redirect:/main?message=Group deleted");
            }
        }
        return new ModelAndView("redirect:/main");
    }
}