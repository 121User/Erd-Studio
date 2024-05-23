package com.example.project.repository;

import com.example.project.model.Entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    @Modifying
    @Query("DELETE FROM Diagram d WHERE d.groupId = :groupId")
    void deleteAllDiagramByGroup(Long groupId);

    //Удаление связей со всеми участниками
    @Modifying
    @Query("DELETE FROM GroupUser gu WHERE gu.group.id = :groupId")
    void deleteAllGroupUserByGroup(Long groupId);
}
