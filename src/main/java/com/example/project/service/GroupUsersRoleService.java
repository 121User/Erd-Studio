package com.example.project.service;

import com.example.project.model.Entity.GroupUsersRole;
import com.example.project.repository.GroupUsersRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class GroupUsersRoleService {
    private final GroupUsersRoleRepository groupUsersRoleRepository;

    @Autowired
    public GroupUsersRoleService(GroupUsersRoleRepository groupUsersRoleRepository) {
        this.groupUsersRoleRepository = groupUsersRoleRepository;
    }

    public GroupUsersRole getById(Long id) {
        return groupUsersRoleRepository.findById(id).get();
    }

    //Роль по умолчанию
    public GroupUsersRole getDefault() {
        return getById((long) 1);
    }
}