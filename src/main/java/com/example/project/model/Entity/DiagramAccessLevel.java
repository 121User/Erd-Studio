package com.example.project.model.Entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "diagram_access_levels")
public class DiagramAccessLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dal_id")
    private Long id;

    @Column(name = "dal_name")
    private String name;
}