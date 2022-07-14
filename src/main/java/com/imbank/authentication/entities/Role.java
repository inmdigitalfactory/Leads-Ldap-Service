package com.imbank.authentication.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

/**
 *
 * This entity details users of the authentication service that are allowed to manage the authentication service
 * They can create, read, update and delete users and/or applications
 */
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "sys_roles")
public class Role extends EntityAuditor {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String description;
    @ManyToMany(fetch = FetchType.EAGER)
    @ToString.Exclude
    private Set<Permission> permissions;
    @ManyToOne
    @JoinColumn(name = "app_id")
    @JsonIgnore
    private AllowedApp app;
    private boolean requiresSpecificApp;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Role role = (Role) o;
        return id != null && Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
