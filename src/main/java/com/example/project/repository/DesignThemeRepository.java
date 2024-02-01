package com.example.project.repository;

import com.example.project.model.Entity.DesignTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DesignThemeRepository extends JpaRepository<DesignTheme, Long> {
    DesignTheme findByName(String name);
}
