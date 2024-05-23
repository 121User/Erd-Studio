package com.example.project.model.Entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "owner_id")
    private Long ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_access_level_id")
    private GroupAccessLevel groupAccessLevel;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "group_id")
    private List<Diagram> diagrams;

    //Форматирование дат (используется при выводе на страницу в th)
    public String getCreationDate() {
        String result = creationDate.toString();
        result = result.split("\\.")[0];
        String[] dateTimeStrings = result.split("T");
        result = dateTimeStrings[1] + " " + dateTimeStrings[0];
        return result;
    }
}