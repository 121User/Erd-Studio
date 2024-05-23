package com.example.project.service;

import com.example.project.model.Entity.DiagramAccessLevel;
import com.example.project.repository.DiagramAccessLevelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class DiagramAccessLevelService {
    private final DiagramAccessLevelRepository diagramAccessLevelRepository;

    @Autowired
    public DiagramAccessLevelService(DiagramAccessLevelRepository diagramAccessLevelRepository) {
        this.diagramAccessLevelRepository = diagramAccessLevelRepository;
    }

    public DiagramAccessLevel getById(Long id) {
        return diagramAccessLevelRepository.findById(id).get();
    }

    //Уровень доступа к диаграмме по умолчанию
    public DiagramAccessLevel getDefault() {
        return getById((long) 1);
    }
}