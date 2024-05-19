package com.example.project.service;

import com.example.project.model.Entity.Diagram;
import com.example.project.model.Dto.DiagramDto;
import com.example.project.model.Entity.User;
import com.example.project.repository.DiagramRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class DiagramService {
    private final DiagramRepository diagramRepository;


    @Autowired
    public DiagramService(DiagramRepository diagramRepository) {
        this.diagramRepository = diagramRepository;
    }

    public Diagram getByID(Long id) {
        return diagramRepository.findById(id).get();
    }

    public Diagram createByName(User user, DiagramDto diagramDto) {
        diagramDto.setName(getDiagramName(user, diagramDto.getName()));

        Diagram diagram = new Diagram();
        diagram.setName(getDiagramName(user, diagramDto.getName()));
        diagram.setCreationDate(diagramDto.getCreationDate());
        diagram.setModifiedDate(diagramDto.getModifiedDate());
        diagram.setCode(diagramDto.getCode());
        diagram.setUserId(user.getId());
        return diagramRepository.save(diagram);
    }

    public void changeName(Diagram diagram, String name, User user){
        if(name != null && !name.equals("") && !diagram.getName().equals(name)){
            diagram.setName(getDiagramName(user, name));
            diagram.setModifiedDate(LocalDateTime.now());
            diagramRepository.save(diagram);
        }
    }
    public void changeCode(Diagram diagram, String code){
        if(code != null && diagram != null){
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

    //Проверка уникальности названия, возврат уникального названия диаграммы
    private String getDiagramName(User user, String diagramName){
        if(user.getDiagrams().size() > 0){
            boolean close = false;
            do {
                for (int i = 0; i < user.getDiagrams().size(); i++) {
                    if (user.getDiagrams().get(i).getName().equals(diagramName)) {
                        diagramName += "-";
                        break;
                    } else if (i == user.getDiagrams().size() - 1) {
                        close = true;
                    }
                }
            } while (!close);
        }
        return diagramName;
    }

    public void deleteDiagram(Long diagramId){
        diagramRepository.delete(getByID(diagramId));
    }
}