package com.example.project.model.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupUserOutputDto {
    //Для передачи данных участника группы на страницу списка участников группы
    private Long id;
    private String name;
    private String role;
    private String entryDate;
}