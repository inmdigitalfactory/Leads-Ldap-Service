package com.imbank.authentication.dtos;

import lombok.Data;

@Data
public class LdapUserDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String name;
}
