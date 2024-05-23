package com.example.project.model.Entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

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


    //Форматирование дат (используется при выводе на страницу в th)
    public String getEntryDate() {
        String result = entryDate.toString();
        result = result.split("\\.")[0];
        String[] dateTimeStrings = result.split("T");
        result = dateTimeStrings[1] + " " + dateTimeStrings[0];
        return result;
    }
}