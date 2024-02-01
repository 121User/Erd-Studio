package com.example.project.service;

import com.example.project.model.Entity.DesignTheme;
import com.example.project.repository.DesignThemeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class DesignThemeService {
    private final DesignThemeRepository designThemeRepository;

    @Autowired
    public DesignThemeService(DesignThemeRepository designThemeRepository) {
        this.designThemeRepository = designThemeRepository;
    }

    public DesignTheme getByName(String name) {
        return designThemeRepository.findByName(name);
    }
}