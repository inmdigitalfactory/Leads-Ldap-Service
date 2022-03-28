package com.imbank.authentication.dtos;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class AuthDTO {
    @NotBlank @NotEmpty @NotNull
    private String username;
    @NotBlank @NotEmpty @NotNull @ToString.Exclude
    private String password;
}
