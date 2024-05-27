package com.example.project.service;

import com.example.project.model.Entity.GroupAccessLevel;
import com.example.project.repository.GroupAccessLevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class GroupAccessLevelService {
    private final GroupAccessLevelRepository groupAccessLevelRepository;

    @Autowired
    public GroupAccessLevelService(GroupAccessLevelRepository groupAccessLevelRepository) {
        this.groupAccessLevelRepository = groupAccessLevelRepository;
    }

    public GroupAccessLevel getById(Long id) {
        return groupAccessLevelRepository.findById(id).get();
    }

    //Роль по умолчанию
    public GroupAccessLevel getDefault() {
        return getById((long) 1);
    }
}