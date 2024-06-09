package com.example.project.model.Entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

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

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "group_id")
    private Long groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagram_access_level_id")
    private DiagramAccessLevel diagramAccessLevel;


    //Форматирование дат (используется при выводе на страницу в th)
    public String getCreationDate() {
//        String result = creationDate.toString();
//        result = result.split("\\.")[0];
//        String[] dateTimeStrings = result.split("T");
//        result = dateTimeStrings[1] + " " + dateTimeStrings[0];
//        return result;
        return getFormattedDateTime(creationDate.toString());
    }
    public String getModifiedDate() {
        if(modifiedDate != null){
//            String result = modifiedDate.toString();
//            result = result.split("\\.")[0];
//            String[] dateTimeStrings = result.split("T");
//            result = dateTimeStrings[1] + " " + dateTimeStrings[0];
//            return result;
            return getFormattedDateTime(modifiedDate.toString());
        }
        return "";
    }
}