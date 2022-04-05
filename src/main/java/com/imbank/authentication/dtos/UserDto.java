package com.imbank.authentication.dtos;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserDto {

    private boolean enabled = true;
    @NotNull @NotBlank @NotEmpty
    private String username;//AD username
    @NotNull @NotBlank @NotEmpty
    private String baseDn;
    @Min(1)
    private long appId;
    private List<RoleDto> roles = new ArrayList<>();
}
