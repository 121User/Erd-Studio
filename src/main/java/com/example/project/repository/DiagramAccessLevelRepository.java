package com.example.project.repository;

import com.example.project.model.Entity.DiagramAccessLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagramAccessLevelRepository extends JpaRepository<DiagramAccessLevel, Long> {
}
