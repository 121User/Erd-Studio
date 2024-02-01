package com.example.project.model.Entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "design_theme")
public class DesignTheme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "theme_id")
    private Long id;

    @Column(name = "theme_name")
    private String name;
}