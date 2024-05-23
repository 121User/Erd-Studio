package com.example.project.service;

import com.example.project.model.Entity.Diagram;
import com.example.project.model.Entity.User;
import com.example.project.repository.DiagramRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@Slf4j
public class DiagramService {
    private final DiagramRepository diagramRepository;
    private final DiagramAccessLevelService diagramAccessLevelService;


    @Autowired
    public DiagramService(DiagramRepository diagramRepository, DiagramAccessLevelService diagramAccessLevelService) {
        this.diagramRepository = diagramRepository;
        this.diagramAccessLevelService = diagramAccessLevelService;
    }

    public Diagram getByID(Long id) {
        return diagramRepository.findById(id).get();
    }

    public Diagram createByName(User user, String groupIdOpt, String diagramName, String diagramCode) {
        Diagram diagram = new Diagram();
        diagram.setName(getUniqueDiagramName(user, diagramName));
        diagram.setCreationDate(LocalDateTime.now());
        diagram.setModifiedDate(LocalDateTime.now());
        diagram.setCode(diagramCode);
        diagram.setOwnerId(user.getId());
        diagram.setDiagramAccessLevel(diagramAccessLevelService.getDefault());
        if(!groupIdOpt.equals("null")){
            diagram.setGroupId(Long.parseLong(groupIdOpt));
        }
        return diagramRepository.save(diagram);
    }

    public void changeName(Diagram diagram, String name, User user) {
        if (name != null && !name.equals("") && !diagram.getName().equals(name)) {
            diagram.setName(getUniqueDiagramName(user, name));
            diagram.setModifiedDate(LocalDateTime.now());
            diagramRepository.save(diagram);
        }
    }

    public void changeCode(Diagram diagram, String code) {
        if (code != null && diagram != null) {
            //Восстановление запрещенных в URL символов
            code = code.replace("*n", "\n")
                    .replace("%7B", "{").replace("%7D", "}")
                    .replace("%5B", "[").replace("%5D", "]")
                    .replace("%2F", "/").replace("%3C", "<");
            diagram.setCode(code);
            diagram.setModifiedDate(LocalDateTime.now());
            diagramRepository.save(diagram);
        }
    }

    public void deleteDiagram(Long diagramId) {
        diagramRepository.delete(getByID(diagramId));
    }

    //Проверка уникальности названия, возврат уникального названия диаграммы
    private String getUniqueDiagramName(User user, String diagramName) {
        int  cloneNumber = 0;
        String result = diagramName;
        //При каждом изменении имени проверка списка на клонов
        if (user.getDiagrams().size() > 0) {
            boolean close = false;
            do {
                for (int i = 0; i < user.getDiagrams().size(); i++) {
                    if (user.getDiagrams().get(i).getName().equals(result)) {
                        cloneNumber += 1;
                        break;
                    } else if (i == user.getDiagrams().size() - 1) {
                        close = true;
                    }
                }
                if(!close && cloneNumber > 0){
                    result = diagramName + cloneNumber;
                }
            } while (!close);
        }
        return result;
    }
}