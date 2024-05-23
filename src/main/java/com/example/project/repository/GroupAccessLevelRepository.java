package com.example.project.repository;

import com.example.project.model.Entity.GroupAccessLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupAccessLevelRepository extends JpaRepository<GroupAccessLevel, Long> {
}
