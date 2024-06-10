package com.example.project.model.Entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.example.project.util.Helper.getFormattedDateTime;

@Entity
@Data
@Table(name = "diagram_history")
public class DiagramHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dh_id")
    private Long id;
    @Column(name = "dh_modified_date")
    private LocalDateTime modifiedDate;

    @Column(name = "dh_name")
    private String name;

    @Column(name = "dh_code")
    private String code;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "diagram_id")
    private Diagram diagram;


    //Форматирование дат для вывода
    public String getModifiedDate() {
        if(modifiedDate != null){
            return getFormattedDateTime(modifiedDate.toString());
        }
        return "";
    }
}