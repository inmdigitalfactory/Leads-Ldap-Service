package com.imbank.authentication.dtos;

import lombok.Data;

import java.util.List;

@Data
public class SystemAccessDto {
    private Long app;
    private Long role;
    private List<Long> appIds; //the applications this access is granted for. This applies only to ldap service
}
