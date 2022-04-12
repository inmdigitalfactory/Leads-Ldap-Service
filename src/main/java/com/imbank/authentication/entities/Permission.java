package com.imbank.authentication.entities;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

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
@Table(name = "sys_permissions")
public class Permission extends EntityAuditor {
    @Id
    @GeneratedValue
    private Long id;

    private String code;
    private String description;
    @OneToOne
    private AllowedApp app;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Permission that = (Permission) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
