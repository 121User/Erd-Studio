package com.example.project.service;

import com.example.project.model.Entity.Group;
import com.example.project.model.Entity.GroupAccessLevel;
import com.example.project.model.Entity.User;
import com.example.project.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupAccessLevelService groupAccessLevelService;


    @Autowired
    public GroupService(GroupRepository groupRepository, GroupAccessLevelService groupAccessLevelService) {
        this.groupRepository = groupRepository;
        this.groupAccessLevelService = groupAccessLevelService;
    }

    public Group getByID(Long id) {
        return getOptByID(id).get();
    }

    //Получение для проверки существования
    public Optional<Group> getOptByID(Long id) {
        return groupRepository.findById(id);
    }

    public Group createByName(User user, String groupName) {
        Group group = new Group();
        group.setName(getUniqueGroupName(user, groupName));
        group.setCreationDate(LocalDateTime.now());
        group.setOwnerId(user.getId());
        group.setGroupAccessLevel(groupAccessLevelService.getDefault());
        return groupRepository.save(group);
    }

    public void changeName(Long groupId, String groupName, User user) {
        Group group = getByID(groupId);
        if (groupName != null && !groupName.equals("") && !group.getName().equals(groupName)) {
            group.setName(getUniqueGroupName(user, groupName));
            groupRepository.save(group);
        }
    }

    public void changeAccessLevel(Group group, Long accessLevelId) {
        GroupAccessLevel groupAccessLevel = groupAccessLevelService.getById(accessLevelId);
        group.setGroupAccessLevel(groupAccessLevel);
        groupRepository.save(group);
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        groupRepository.deleteAllDiagramByGroup(groupId);
        groupRepository.deleteAllGroupUserByGroup(groupId);
        groupRepository.delete(getByID(groupId));
    }

    //Проверка уникальности названия, возврат уникального названия группы
    private String getUniqueGroupName(User user, String groupName) {
        int cloneNumber = 0;
        String result = groupName;
        //При каждом изменении имени проверка списка на клонов
        if (user.getGroups().size() > 0) {
            boolean close = false;
            do {
                for (int i = 0; i < user.getGroups().size(); i++) {
                    if (user.getGroups().get(i).getName().equals(result)) {
                        cloneNumber += 1;
                        break;
                    } else if (i == user.getGroups().size() - 1) {
                        close = true;
                    }
                }
                if (!close && cloneNumber > 0) {
                    result = groupName + cloneNumber;
                }
            } while (!close);
        }
        return result;
    }

    //Получение Url для входа в группу
    public String getGroupUrl(Long groupId) {
        return "/group/" + groupId + "/connect";
    }
}