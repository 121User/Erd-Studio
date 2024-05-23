package com.example.project.model.Entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "group_access_levels")
public class GroupAccessLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gal_id")
    private Long id;

    @Column(name = "gal_name")
    private String name;
}