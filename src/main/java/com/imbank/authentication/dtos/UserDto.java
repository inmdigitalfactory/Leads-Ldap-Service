package com.imbank.authentication.dtos;

import lombok.Data;

@Data
public class UserDto {

    private boolean enabled = true;
    private String username;//AD username
    private String ou;
    private long appId;
}
