package com.example.project.service;

import com.example.project.model.Entity.Diagram;
import com.example.project.model.Entity.DiagramHistory;
import com.example.project.model.Entity.User;
import com.example.project.repository.DiagramHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class DiagramHistoryService {
    private final DiagramHistoryRepository diagramHistoryRepository;

    @Autowired
    public DiagramHistoryService(DiagramHistoryRepository diagramHistoryRepository) {
        this.diagramHistoryRepository = diagramHistoryRepository;
    }

    public DiagramHistory getByID(Long id) {
        return diagramHistoryRepository.findById(id).get();
    }

    public void create(Diagram diagram, User user) {
        DiagramHistory diagramHistory = new DiagramHistory();
        diagramHistory.setModifiedDate(LocalDateTime.now());
        diagramHistory.setName(diagram.getName());
        diagramHistory.setCode(diagram.getCode());
        diagramHistory.setDiagram(diagram);
        diagramHistory.setUser(user);
        diagramHistoryRepository.save(diagramHistory);
    }
}