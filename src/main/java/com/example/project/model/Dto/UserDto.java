package com.example.project.model.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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