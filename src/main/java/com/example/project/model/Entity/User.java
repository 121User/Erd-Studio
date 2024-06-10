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
    @Column(name = "u_id")
    private Long id;

    @Column(name = "u_name")
    private String name;

    @Column(name = "u_email")
    private String email;

    @Column(name = "u_password")
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "design_theme_id")
    private DesignTheme designTheme;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Diagram> diagrams;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Group> groups;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<GroupUser> groupUsers;
}