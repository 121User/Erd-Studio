package com.example.project.model.Entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.project.util.Helper.getFormattedDateTime;

@Entity
@Data
@Table(name = "diagrams")
public class Diagram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "d_id")
    private Long id;

    @Column(name = "d_name")
    private String name;
    @Column(name = "d_creation_date")
    private LocalDateTime creationDate;
    @Column(name = "d_modified_date")
    private LocalDateTime modifiedDate;

    @Column(name = "d_code")
    private String code;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagram_access_level_id")
    private DiagramAccessLevel diagramAccessLevel;

    @OneToMany(mappedBy = "diagram", cascade = CascadeType.ALL)
    private List<DiagramHistory> diagramHistories;


    //Форматирование дат для вывода
    public String getCreationDate() {
        return getFormattedDateTime(creationDate.toString());
    }
    public String getModifiedDate() {
        if(modifiedDate != null){
            return getFormattedDateTime(modifiedDate.toString());
        }
        return "";
    }
}