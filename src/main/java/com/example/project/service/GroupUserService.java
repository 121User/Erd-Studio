package com.example.project.service;

import com.example.project.model.Dto.GroupUserOutputDto;
import com.example.project.model.Entity.Group;
import com.example.project.model.Entity.GroupUser;
import com.example.project.model.Entity.GroupUsersRole;
import com.example.project.model.Entity.User;
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
        for (GroupUser groupUser : user.getGroupUsers()) {
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

    //Проверка наличия пользователя в группе
    public Boolean checkUserInGroup(User user, Group group) {
        Optional<GroupUser> groupUserOpt = getByUserAndGroup(user, group);
        return group.getOwner().getId().equals(user.getId()) || groupUserOpt.isPresent();
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
}