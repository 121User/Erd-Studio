package com.example.project.model.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GroupOutputDto {
    //Для передачи данных группы на страницу списка групп
    private Long id;
    private String name;
    private String userName;

    private String creationDate;

    private String accessLevel;
    private String connectionLink;
}