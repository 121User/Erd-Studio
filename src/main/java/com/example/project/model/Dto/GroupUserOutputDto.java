package com.example.project.model.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupUserOutputDto {
    private Long id;
    private String name;
    private String role;
    private String entryDate;
}