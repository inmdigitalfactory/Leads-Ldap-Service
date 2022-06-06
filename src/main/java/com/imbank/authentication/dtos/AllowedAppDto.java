package com.imbank.authentication.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class AllowedAppDto {
    @NotNull @NotEmpty @NotBlank
    private String name;
//    @Min(1)
    private long tokenValiditySeconds = 1800;
//    @Min(1)
    private long refreshTokenValiditySeconds = 7200;
//    @NotNull
    private Boolean enabled = true;
}
