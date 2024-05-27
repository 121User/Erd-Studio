package com.example.project.controller;

import com.example.project.model.Dto.GroupOutputDto;
import com.example.project.model.Dto.GroupUserOutputDto;
import com.example.project.model.Entity.Group;
import com.example.project.model.Entity.GroupUser;
import com.example.project.model.Entity.User;
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
import java.util.List;
import java.util.Optional;

import static com.example.project.util.Helper.encryptUrl;
import static com.example.project.util.ListProcessingUtil.filterGroupUserListBySearch;
import static com.example.project.util.ListProcessingUtil.sortGroupUserListByEntryDate;
import static com.example.project.util.SessionUtil.getLongAttrFromSession;

@RestController
@RequestMapping("/group/{groupId}/participant/list")
public class GroupParticipantsController {
    private final UserService userService;
    private final GroupService groupService;
    private final GroupUserService groupUserService;

    @Autowired
    public GroupParticipantsController(UserService userService, GroupService groupService, GroupUserService groupUserService) {
        this.userService = userService;
        this.groupService = groupService;
        this.groupUserService = groupUserService;
    }

    @RequestMapping("")
    public ModelAndView viewGroupParticipantListPage(@PathVariable(name = "groupId") Long groupId,
                                                     @RequestParam(name = "searchText", required = false) String searchText,
                                                     HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        ModelAndView modelAndView;
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Group group = groupService.getByID(groupId);

            //Создание объекта группы для вывода
            GroupOutputDto groupOutputDto = new GroupOutputDto(group.getId(), group.getName(),
                    userService.getById(group.getOwnerId()).get().getName(), null,
                    group.getGroupAccessLevel().getName(), encryptUrl(groupService.getGroupUrl(group.getId())));

            modelAndView = new ModelAndView("group_participant_list_page");
            modelAndView.addObject("userName", user.getName());
            modelAndView.addObject("group", groupOutputDto);
            modelAndView.addObject("searchText", searchText);

            List<GroupUser> groupUserList = sortGroupUserListByEntryDate(groupUserService.getAllByGroup(group)); //Отсортированный список пользователей группы
            //Поиск по названию диаграммы
            if (searchText != null) {
                groupUserList = filterGroupUserListBySearch(groupUserList, searchText);
            }
            //Вывод сообщения, если список диаграмм пуст
            if (groupUserList.isEmpty()) {
                modelAndView.addObject("listInfo", "Список участников пуст");
            } else {
                //Получение обработанного списка пользователей группы для вывода
                List<GroupUserOutputDto> groupUserOutputDtoList = groupUserService.getGroupUserOutputDtoList(groupUserList);
                modelAndView.addObject("groupUserList", groupUserOutputDtoList);
            }
        } else {
            modelAndView = new ModelAndView("redirect:/main");
        }
        return modelAndView;
    }

    @RequestMapping("/change-role/{userId}")
    public ModelAndView changeGroupUserRole(@PathVariable(name = "groupId") Long groupId,
                                            @PathVariable(name = "userId") Long userIdForChangeRole,
                                            @RequestParam(name = "role") Long role,
                                            HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            User user = userService.getById(userIdForChangeRole).get();
            Group group = groupService.getByID(groupId);
            //Проверка на соответствие авторизованного пользователя владельцу группы
            if (userId.equals(group.getOwnerId())) {
                GroupUser groupUser = groupUserService.getByUserAndGroup(user, group).get();
                groupUserService.changeRole(groupUser, role);
            }
        }
        return new ModelAndView("redirect:/group/" + groupId + "/participant/list");
    }

    @RequestMapping("/delete/{userId}")
    public ModelAndView deleteGroupUser(@PathVariable(name = "groupId") Long groupId,
                                        @PathVariable(name = "userId") Long userIdForDelete,
                                        HttpServletRequest request) {
        Long userId = getLongAttrFromSession(request, "userId");
        Optional<User> userOpt = userService.getById(userId);
        //Проверка существования пользователя в системе
        if (userOpt.isPresent()) {
            Group group = groupService.getByID(groupId);
            //Проверка на соответствие авторизованного пользователя владельцу группы или администратору
            if (userId.equals(group.getOwnerId())) {
                User userForDelete = userService.getById(userIdForDelete).get();
                GroupUser groupUser = groupUserService.getByUserAndGroup(userForDelete, group).get();
                groupUserService.deleteGroupUser(groupUser);
            }
        }
        return new ModelAndView("redirect:/group/" + groupId + "/participant/list");
    }
}