package com.example.project.model.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiagramOutputDto {
    //Для передачи данных диаграммы на страницу списка диаграмм группы
    private Long id;
    private String name;
    private String userName;
    private String creationDate;
    private String modifiedDate;

    private String code;

    private String accessLevel;
    private String connectionLink;
}