package com.example.project.model.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DiagramOutputDto {
    //Используется для вывода списка диаграмм в группе
    private Long id;
    private String name;
    private String userName;
    private String creationDate;
    private String modifiedDate;
}