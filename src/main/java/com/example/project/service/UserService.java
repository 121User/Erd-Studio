package com.example.project.service;

import com.example.project.model.Entity.DesignTheme;
import com.example.project.model.Entity.Diagram;
import com.example.project.model.Entity.Group;
import com.example.project.model.Entity.User;
import com.example.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.example.project.util.ListProcessingUtil.filterDiagramListByOwner;


@Service
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

    public List<Diagram> getPrivateDiagramList(User user) {
        List<Diagram> diagramList = new ArrayList<>();
        for(Diagram d : user.getDiagrams()){
            if(d.getGroupId() == null){
                diagramList.add(d);
            }
        }
        return diagramList;
    }

    public void createUser(String email, String password, String designThemeName) {
        DesignTheme designTheme = designThemeService.getByName(designThemeName);
        User user = new User();
        user.setEmail(email);
        user.setName(generateUserName());
        user.setPassword(password);
        user.setDesignTheme(designTheme);
        userRepository.save(user);
    }

    public Long addDiagram(User user, String groupIdOpt, String diagramName, String diagramCode) {
        Long groupId = null;
        if (!groupIdOpt.equals("null")) {
            groupId = Long.parseLong(groupIdOpt);
        }
        List<Diagram> diagramList = getDiagramListForUniqueName(user, groupId);
        Diagram diagram = diagramService.createByName(user, groupId, diagramList, diagramName, diagramCode);
        return diagram.getId();
    }

    //Получение списка диаграмм для проверки уникальности названия
    public List<Diagram> getDiagramListForUniqueName(User user, Long groupId) {
        List<Diagram> diagramList = getPrivateDiagramList(user);
        if(groupId != null){
            Optional<Group> groupOpt = groupService.getOptByID(groupId);
            if (groupOpt.isPresent()) {
                Group group = groupOpt.get();
                diagramList = filterDiagramListByOwner(group.getDiagrams(), user.getId());
            }
        }
        return diagramList;
    }

    public Long addGroup(User user, String groupName) {
        Group group = groupService.createByName(user, groupName);
        return group.getId();
    }

    public String changeName(Long id, String name) {
        String curName = getById(id).get().getName();
        if(curName.equals(name)){
            return null;
        } else if (getById(id).isPresent() && checkNameUniqueness(name)) {
            if(name.length() >= 5) {
                User user = getById(id).get();
                user.setName(name);
                userRepository.save(user);
                return "ok";
            }
            return "short";
        }
        return "occupied";
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
            userRepository.delete(getById(userId).get());
        }
    }

    //Удаление собственной группы
    public void deleteGroup(Long groupId) {
        groupService.deleteGroup(groupId);
    }

    //Удаление всех групп
    public void deleteAllGroups(Long userId) {
        User user = getById(userId).get();
        for(Group group: user.getGroups()){
            groupService.deleteGroup(group.getId());
        }
    }

    //Генерация уникального имени пользователя
    private String generateUserName() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int nameLength = 8;
        StringBuilder name = new StringBuilder(nameLength);

        do{
            for (int i = 0; i < nameLength; i++) {
                Random random = new Random();
                int randomIndex = random.nextInt(characters.length());
                name.append(characters.charAt(randomIndex));
            }
        } while (!checkNameUniqueness(name.toString()));
        return name.toString();
    }

    //Проверка уникальности имени пользователя
    private Boolean checkNameUniqueness(String name) {
        List<User> userList = userRepository.findAll();
        for(User user: userList){
            if(user.getName().equals(name)){
                return false;
            }
        }
        return true;
    }
}