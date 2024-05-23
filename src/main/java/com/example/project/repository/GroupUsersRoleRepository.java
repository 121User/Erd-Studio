package com.example.project.repository;

import com.example.project.model.Entity.GroupUsersRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupUsersRoleRepository extends JpaRepository<GroupUsersRole, Long> {
}
