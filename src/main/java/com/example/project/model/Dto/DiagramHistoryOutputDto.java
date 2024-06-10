package com.example.project.model.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiagramHistoryOutputDto {
    //Для передачи данных версии диаграммы для списка истории диаграммы
    private Long id;
    private String userName;
    private String modifiedDate;
}