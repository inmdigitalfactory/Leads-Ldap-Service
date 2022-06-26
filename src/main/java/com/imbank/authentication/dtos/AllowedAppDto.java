package com.imbank.authentication.dtos;

import com.imbank.authentication.enums.AuthModule;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AllowedAppDto {
    @NotNull @NotEmpty @NotBlank
    private String name;
    private long tokenValiditySeconds = 1800;
    private long refreshTokenValiditySeconds = 7200;
    private Boolean enabled = true;
    private List<AuthModule> modules;
}
