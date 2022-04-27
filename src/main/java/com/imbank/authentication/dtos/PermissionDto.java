package com.imbank.authentication.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class PermissionDto {
    @NotNull @NotBlank @NotEmpty
    private String code;
    private String description;
}
