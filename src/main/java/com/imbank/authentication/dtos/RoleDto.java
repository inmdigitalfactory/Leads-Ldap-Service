package com.imbank.authentication.dtos;

import lombok.Data;

import javax.validation.constraints.Min;
import java.util.List;

@Data
public class RoleDto {
    @Min(1)
    private Long role;
    private List<Long> apps;
}
