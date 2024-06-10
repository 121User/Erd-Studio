package com.example.project.repository;

import com.example.project.model.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    //Удаление связей со всеми группами
    @Modifying
    @Query("DELETE FROM GroupUser gu WHERE gu.user.id = :userId")
    void deleteAllGroupUserByUser(Long userId);
}
