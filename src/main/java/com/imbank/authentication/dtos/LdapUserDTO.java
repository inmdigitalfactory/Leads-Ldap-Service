package com.imbank.authentication.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.imbank.authentication.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LdapUserDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String name;
    private String department;
    private String description;
    private String baseDn;
    @JsonIgnore
    private User user;
}
