package com.example.project.service;

import com.example.project.model.Entity.DesignTheme;
import com.example.project.model.Entity.Diagram;
import com.example.project.model.Entity.Group;
import com.example.project.model.Entity.User;
import com.example.project.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final DesignThemeService designThemeService;
    private final DiagramService diagramService;
    private final GroupService groupService;


    @Autowired
    public UserService(UserRepository userRepository, DesignThemeService designThemeService,
                       DiagramService diagramService, GroupService groupService) {
        this.userRepository = userRepository;
        this.designThemeService = designThemeService;
        this.diagramService = diagramService;
        this.groupService = groupService;
    }

    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    public String getNameById(Long id) {
        if (getById(id).isPresent()) {
            User user = getById(id).get();
            return user.getName();
        }
        return null;
    }

    public Optional<User> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void createUser(String email, String password, String designThemeName) {
        DesignTheme designTheme = designThemeService.getByName(designThemeName);
        User user = new User();
        user.setEmail(email);
        user.setName(email.split("@")[0]);
        user.setPassword(password);
        user.setDesignTheme(designTheme);
        userRepository.save(user);
    }

    public Long addDiagram(User user, String groupIdOpt, String diagramName, String diagramCode) {
        Diagram diagram = diagramService.createByName(user, groupIdOpt, diagramName, diagramCode);
        return diagram.getId();
    }

    public Long addGroup(User user, String groupName) {
        Group group = groupService.createByName(user, groupName);
        return group.getId();
    }

    public void changePassword(Long id, String password) {
        if (getById(id).isPresent()) {
            User user = getById(id).get();
            user.setPassword(password);
            userRepository.save(user);
        }
    }

    public void changeDesignTheme(User user, String designTheme) {
        if (designTheme != null) {
            user.setDesignTheme(designThemeService.getByName(designTheme));
            userRepository.save(user);
        }
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (getById(userId).isPresent()) {
            userRepository.deleteAllDiagramByUser(userId);
            userRepository.deleteAllGroupUserByUser(userId);
            userRepository.deleteAllGroupByUser(userId);
            userRepository.delete(getById(userId).get());
        }
    }

    public void deleteDiagram(Long userId, Long diagramId) {
        //Проверка является ли отправитель запроса владельцем диаграммы
        if (diagramService.getByID(diagramId).getOwnerId().equals(userId)) {
            diagramService.deleteDiagram(diagramId);
        }
    }

    //Удаление собственной группы
    public void deleteGroup(Long userId, Long groupId) {
        //Проверка является ли отправитель запроса владельцем группы
        if (groupService.getByID(groupId).getOwnerId().equals(userId)) {
            groupService.deleteGroup(groupId);
        }
    }
}