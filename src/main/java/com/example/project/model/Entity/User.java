package com.example.project.model.Entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_email")
    private String email;
    @Column(name = "user_password")
    private String password;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_design_theme")
    private DesignTheme designTheme;


    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private List<Diagram> diagrams;
}