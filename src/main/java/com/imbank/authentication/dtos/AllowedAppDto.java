package com.imbank.authentication.dtos;

import lombok.Data;

@Data
public class AllowedAppDto {
    private String name;
    private long tokenValiditySeconds;
    private long refreshTokenValiditySeconds;
    private boolean enabled;
}
