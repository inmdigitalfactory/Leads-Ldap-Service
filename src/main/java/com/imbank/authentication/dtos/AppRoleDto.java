package com.imbank.authentication.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class AppRoleDto {
    @NotEmpty @NotNull @NotBlank
    private String name;
    private String description;
}
