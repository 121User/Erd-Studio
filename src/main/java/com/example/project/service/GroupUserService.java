package com.example.project.service;

import com.example.project.model.Dto.DiagramOutputDto;
import com.example.project.model.Dto.GroupOutputDto;
import com.example.project.model.Dto.GroupUserOutputDto;
import com.example.project.model.Entity.*;
import com.example.project.repository.GroupUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class GroupUserService {
    private final GroupUserRepository groupUserRepository;
    private final UserService userService;
    private final GroupService groupService;
    private final GroupUsersRoleService groupUsersRoleService;


    @Autowired
    public GroupUserService(GroupUserRepository groupUserRepository, UserService userService,
                            GroupService groupService, GroupUsersRoleService groupUsersRoleService) {
        this.groupUserRepository = groupUserRepository;
        this.userService = userService;
        this.groupService = groupService;
        this.groupUsersRoleService = groupUsersRoleService;
    }

    public Optional<GroupUser> getByUserAndGroup(User user, Group group) {
        return groupUserRepository.findByUserAndGroup(user, group);
    }

    public List<GroupUser> getAllByUser(User user) {
        return groupUserRepository.findAllByUser(user);
    }

    public List<GroupUser> getAllByGroup(Group group) {
        return groupUserRepository.findAllByGroup(group);
    }


    public GroupUser create(Group group, User user) {
        GroupUser groupUser = new GroupUser();
        groupUser.setGroup(group);
        groupUser.setUser(user);
        groupUser.setEntryDate(LocalDateTime.now());
        groupUser.setGroupUsersRole(groupUsersRoleService.getDefault());
        return groupUserRepository.save(groupUser);
    }

    public void changeRole(GroupUser groupUser, Long roleId) {
        GroupUsersRole groupUsersRole = groupUsersRoleService.getById(roleId);
        groupUser.setGroupUsersRole(groupUsersRole);
        groupUserRepository.save(groupUser);
    }

    public void deleteGroupUser(GroupUser groupUser) {
        groupUserRepository.delete(groupUser);
    }

    //Выход из группы
    public void leaveGroup(Long userId, Long groupId) {
        User user = userService.getById(userId).get();
        Group group = groupService.getByID(groupId);
        Optional<GroupUser> groupUserOpt = getByUserAndGroup(user, group);
        if (groupUserOpt.isPresent()) {
            GroupUser groupUser = groupUserOpt.get();
            deleteGroupUser(groupUser);
        }
    }


    //Получение всех групп, в которые вошел пользователь
    public List<Group> getAllGroupsByUser(User user) {
        List<Group> groupList = new ArrayList<>();
        for (GroupUser groupUser : getAllByUser(user)) {
            groupList.add(groupUser.getGroup());
        }
        return groupList;
    }

    //Получение роли пользователя в группе
    public String getUserRoleInGroup(User user, Group group) {
        Optional<GroupUser> groupUserOpt = getByUserAndGroup(user, group);
        String role = ""; //Определение роли пользователя
        if (groupUserOpt.isPresent()) {
            GroupUser groupUser = groupUserOpt.get();
            role = groupUser.getGroupUsersRole().getName();
        }
        return role;
    }

    //Получение списка групп для вывода
    public List<GroupOutputDto> getGroupOutputDtoList(List<Group> groupList) {
        List<GroupOutputDto> groupOutputDtoList = new ArrayList<>();
        for (Group group : groupList) {
            String userName = userService.getById(group.getOwnerId()).get().getName();
            GroupOutputDto groupOutputDto = new GroupOutputDto(group.getId(), group.getName(), userName,
                    group.getCreationDate(), null, null);
            groupOutputDtoList.add(groupOutputDto);
        }
        return groupOutputDtoList;
    }

    //Получение списка пользователей группы для вывода
    public List<GroupUserOutputDto> getGroupUserOutputDtoList(List<GroupUser> groupUserList) {
        List<GroupUserOutputDto> groupUserOutputDtoList = new ArrayList<>();
        for (GroupUser groupUser : groupUserList) {
            GroupUserOutputDto groupUserOutputDto = new GroupUserOutputDto(
                    groupUser.getUser().getId(),
                    groupUser.getUser().getName(),
                    groupUser.getGroupUsersRole().getName(),
                    groupUser.getEntryDate()
            );
            groupUserOutputDtoList.add(groupUserOutputDto);
        }
        return groupUserOutputDtoList;
    }

    //Получение списка диаграмм группы для вывода
    public List<DiagramOutputDto> getDiagramOutputDtoList(List<Diagram> diagramList) {
        List<DiagramOutputDto> diagramOutputDtoList = new ArrayList<>();
        for (Diagram diagram : diagramList) {
            String userName = userService.getById(diagram.getOwnerId()).get().getName();
            DiagramOutputDto diagramOutputDto = new DiagramOutputDto(
                    diagram.getId(), diagram.getName(), userName, diagram.getCreationDate(), diagram.getModifiedDate(),
                    null, null, null
            );
            diagramOutputDtoList.add(diagramOutputDto);
        }
        return diagramOutputDtoList;
    }
}