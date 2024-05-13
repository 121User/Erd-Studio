package com.example.project.service;

import com.example.project.model.Dto.DiagramDto;
import com.example.project.model.Entity.DesignTheme;
import com.example.project.model.Entity.Diagram;
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


    @Autowired
    public UserService(UserRepository userRepository, DesignThemeService designThemeService,
                       DiagramService diagramService) {
        this.userRepository = userRepository;
        this.designThemeService = designThemeService;
        this.diagramService = diagramService;
    }

    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    public String getEmailById(Long id) {
        if (getById(id).isPresent()) {
            User user = getById(id).get();
            return user.getEmail();
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
//        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setPassword(password);
        user.setDesignTheme(designTheme);
        userRepository.save(user);
    }

    public Long addDiagram(User user, DiagramDto diagramDto) {
        Diagram diagram = diagramService.createByName(user, diagramDto);
        return diagram.getId();
    }

    public void changePassword(String email, String password) {
        if (getByEmail(email).isPresent()) {
            User user = getByEmail(email).get();
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

    public void deleteDiagram(Long userId, Long diagramId) {
        //Проверка является ли отправитель запроса владельцем диаграммы
        if(diagramService.getByID(diagramId).getUserId().equals(userId)){
            diagramService.deleteDiagram(diagramId);
        }
    }

    @Transactional
    public void deleteUser(Long userId) {
        if(getById(userId).isPresent()) {
            userRepository.deleteAllDiagramByUser(userId);
            userRepository.delete(getById(userId).get());
        }
    }
}