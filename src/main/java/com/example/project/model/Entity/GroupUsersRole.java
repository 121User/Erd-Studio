package com.example.project.model.Entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "group_users_roles")
public class GroupUsersRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gur_id")
    private Long id;

    @Column(name = "gur_name")
    private String name;
}