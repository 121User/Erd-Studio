package com.example.project.service;

import com.example.project.model.Dto.DiagramHistoryOutputDto;
import com.example.project.model.Dto.DiagramOutputDto;
import com.example.project.model.Entity.*;
import com.example.project.repository.DiagramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class DiagramService {
    private final DiagramRepository diagramRepository;
    private final DiagramAccessLevelService diagramAccessLevelService;
    private final DiagramHistoryService diagramHistoryService;

    @Autowired
    public DiagramService(DiagramRepository diagramRepository, DiagramAccessLevelService diagramAccessLevelService,
                          DiagramHistoryService diagramHistoryService) {
        this.diagramRepository = diagramRepository;
        this.diagramAccessLevelService = diagramAccessLevelService;
        this.diagramHistoryService = diagramHistoryService;
    }

    public Optional<Diagram> getOptByID(Long id) {
        return diagramRepository.findById(id);
    }

    public Diagram createByName(User user, Group group,
                                List<Diagram> diagramList, String diagramName, String diagramCode) {
        Diagram diagram = new Diagram();
        diagram.setName(getUniqueDiagramName(diagramList, diagramName));
        diagram.setCreationDate(LocalDateTime.now());
        diagram.setModifiedDate(LocalDateTime.now());
        diagram.setCode(diagramCode);
        diagram.setOwner(user);
        diagram.setGroup(group);
        diagram.setDiagramAccessLevel(diagramAccessLevelService.getDefault());
        diagramRepository.save(diagram);
        diagramHistoryService.create(diagram, user); //Создание записи в истории диаграммы
        return diagram;
    }

    public void change(Diagram diagram, String name, String code, List<Diagram> diagramList, User user) {
        Boolean changeNameResult = changeName(diagram, name, diagramList);
        Boolean changeCodeResult = changeCode(diagram, code);
        if (changeNameResult || changeCodeResult) {
            //Создание записи в истории диаграммы
            diagramHistoryService.create(diagram, user);
        }
    }

    private Boolean changeName(Diagram diagram, String name, List<Diagram> diagramList) {
        if (!name.equals("") && !diagram.getName().equals(name)) {
            diagram.setName(getUniqueDiagramName(diagramList, name));
            diagram.setModifiedDate(LocalDateTime.now());
            diagramRepository.save(diagram);
            return true;
        }
        return false;
    }

    private Boolean changeCode(Diagram diagram, String code) {
        //Восстановление запрещенных в URL символов
        code = code.replace("*n", "\n")
                .replace("%7B", "{").replace("%7D", "}")
                .replace("%5B", "[").replace("%5D", "]")
                .replace("%2F", "/").replace("%3C", "<");
        String oldCode = diagram.getCode();
        if (oldCode == null || !oldCode.equals(code)) {
            diagram.setCode(code);
            diagram.setModifiedDate(LocalDateTime.now());
            diagramRepository.save(diagram);
            return true;
        }
        return false;
    }

    public void changeAccessLevel(Diagram diagram, Long accessLevelId) {
        DiagramAccessLevel diagramAccessLevel = diagramAccessLevelService.getById(accessLevelId);
        diagram.setDiagramAccessLevel(diagramAccessLevel);
        diagramRepository.save(diagram);
    }

    public void rollback(Diagram diagram, Long diagramHistoryId) {
        DiagramHistory diagramHistory = diagramHistoryService.getByID(diagramHistoryId);
        diagram.setModifiedDate(LocalDateTime.now());
        diagram.setName(diagramHistory.getName());
        diagram.setCode(diagramHistory.getCode());
        diagramRepository.save(diagram);
    }

    public void deleteDiagram(Long diagramId) {
        Optional<Diagram> diagramOpt = getOptByID(diagramId);
        //Проверка существования диаграммы
        if (diagramOpt.isPresent()) {
            Diagram diagram = diagramOpt.get();
            diagramRepository.delete(diagram);
        }
    }

    //Получение списка диаграмм группы для вывода
    public List<DiagramOutputDto> getDiagramOutputDtoList(List<Diagram> diagramList) {
        List<DiagramOutputDto> diagramOutputDtoList = new ArrayList<>();
        for (Diagram diagram : diagramList) {
            String userName = diagram.getOwner().getName();
            DiagramOutputDto diagramOutputDto = new DiagramOutputDto(
                    diagram.getId(), diagram.getName(), userName, diagram.getCreationDate(), diagram.getModifiedDate(),
                    null, null, null
            );
            diagramOutputDtoList.add(diagramOutputDto);
        }
        return diagramOutputDtoList;
    }

    //Получение списка версий из истории диаграммы для вывода
    public List<DiagramHistoryOutputDto> getDiagramHistoryOutputDtoList(List<DiagramHistory> diagramHistoryList) {
        List<DiagramHistoryOutputDto> diagramHistoryOutputDtoList = new ArrayList<>();
        for (DiagramHistory diagramHistory : diagramHistoryList) {
            String userName = diagramHistory.getUser().getName();
            DiagramHistoryOutputDto diagramHistoryOutputDto = new DiagramHistoryOutputDto(
                    diagramHistory.getId(), userName, diagramHistory.getModifiedDate());
            diagramHistoryOutputDtoList.add(diagramHistoryOutputDto);
        }
        return diagramHistoryOutputDtoList;
    }

    //Проверка уникальности названия, возврат уникального названия диаграммы
    private String getUniqueDiagramName(List<Diagram> diagramList, String diagramName) {
        int cloneNumber = 0;
        String result = diagramName;
        //При каждом изменении имени происходит новая проверка списка на уникальность
        if (diagramList.size() > 0) {
            boolean close = false;
            do {
                for (int i = 0; i < diagramList.size(); i++) {
                    if (diagramList.get(i).getName().equals(result)) {
                        cloneNumber += 1;
                        break;
                    } else if (i == diagramList.size() - 1) {
                        close = true;
                    }
                }
                if (!close && cloneNumber > 0) {
                    result = diagramName + cloneNumber;
                }
            } while (!close);
        }
        return result;
    }

    //Получение Url для подключения к диаграмме
    public String getDiagramUrl(Long diagramId) {
        return "/diagram/" + diagramId + "/connect";
    }
}