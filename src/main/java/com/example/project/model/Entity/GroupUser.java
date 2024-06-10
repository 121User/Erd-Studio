package com.example.project.model.Entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.example.project.util.Helper.getFormattedDateTime;

@Entity
@Data
@Table(name = "group_users")
public class GroupUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gu_id")
    private Long id;

    @Column(name = "gu_entry_date")
    private LocalDateTime entryDate;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private GroupUsersRole groupUsersRole;


    //Форматирование дат для вывода
    public String getEntryDate() {
        return getFormattedDateTime(entryDate.toString());
    }
}