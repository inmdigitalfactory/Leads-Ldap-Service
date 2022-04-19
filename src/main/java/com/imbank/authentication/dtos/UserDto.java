package com.imbank.authentication.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class UserDto {

    private boolean enabled = true;
    @NotNull @NotBlank @NotEmpty
    private String username;//AD username
//    @NotNull @NotBlank @NotEmpty
//    private String baseDn;
//    @Min(1)
//    private long appId;
//    private List<RoleDto> roles = new ArrayList<>();
}
