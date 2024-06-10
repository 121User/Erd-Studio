package com.example.project.model.Entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.project.util.Helper.getFormattedDateTime;

@Entity
@Data
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "g_id")
    private Long id;

    @Column(name = "g_name")
    private String name;
    @Column(name = "g_creation_date")
    private LocalDateTime creationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_access_level_id")
    private GroupAccessLevel groupAccessLevel;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<Diagram> diagrams;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<GroupUser> groupUsers;

    //Форматирование дат для вывода
    public String getCreationDate() {
        return getFormattedDateTime(creationDate.toString());
    }
}