package com.example.project.model.Entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

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

    @Column(name = "user_id")
    private Long userId;


    //Форматирование дат (используется при выводе на страницу в th)
    public String getCreationDate() {
        String result = creationDate.toString();
        result = result.replace('T', ' ');
        result = result.split("\\.")[0];
        return result;
    }
    public String getModifiedDate() {
        if(modifiedDate != null){
            String result = modifiedDate.toString();
            result = result.replace('T', ' ');
            result = result.split("\\.")[0];
            return result;
        }
        return "";
    }
}