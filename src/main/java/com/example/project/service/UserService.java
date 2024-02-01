package com.example.project.service;

import com.example.project.model.Dto.DiagramDto;
import com.example.project.model.Entity.DesignTheme;
import com.example.project.model.Dto.UserDto;
import com.example.project.model.Entity.Diagram;
import com.example.project.model.Entity.User;
import com.example.project.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final DesignThemeService designThemeService;
    private final DiagramService diagramService;


    @Autowired
    public UserService(UserRepository userRepository, DesignThemeService designThemeService, DiagramService diagramService) {
        this.userRepository = userRepository;
        this.designThemeService = designThemeService;
        this.diagramService = diagramService;
    }

    public Optional<User> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void createUser(UserDto userDto) {
        DesignTheme designTheme = designThemeService.getByName(userDto.getDesignTheme());

        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setDesignTheme(designTheme);

        userRepository.save(user);
    }

    public Long addDiagram(String email, DiagramDto diagramDto) {
        if (getByEmail(email).isPresent()) {
            User user = getByEmail(email).get();
            Diagram diagram = diagramService.createByName(diagramDto, user);
            return diagram.getId();
        }
        return null;
    }

    public void changePassword(String email, String password) {
        if (getByEmail(email).isPresent()) {
            User user = getByEmail(email).get();
            user.setPassword(password);
            userRepository.save(user);
        }
    }

    public void changeDesignTheme(String email, String designTheme) {
        if (getByEmail(email).isPresent()) {
            User user = getByEmail(email).get();
            user.setDesignTheme(designThemeService.getByName(designTheme));
            userRepository.save(user);
        }
    }

    public void deleteDiagram(Long diagramId) {
        diagramService.deleteDiagram(diagramId);
    }

    @Transactional
    public void deleteUser(User user) {
        userRepository.deleteAllDiagramByUser(user.getId());
        userRepository.delete(user);
    }
}