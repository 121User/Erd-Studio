package com.example.project.repository;

import com.example.project.model.Entity.Group;
import com.example.project.model.Entity.GroupUser;
import com.example.project.model.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupUserRepository extends JpaRepository<GroupUser, Long> {
    Optional<GroupUser> findByUserAndGroup(User user, Group group);
}
