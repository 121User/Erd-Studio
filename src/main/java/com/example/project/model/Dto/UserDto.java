package com.example.project.model.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
    private String email;
    private String password;
    private String designTheme;

    public String getDesignTheme() {
        return designTheme;
    }
}