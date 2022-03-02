package com.imbank.authentication.entities;

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
@Table(name = "users", indexes = {
        @Index(name = "username", columnList = "username")
})
public class User extends EntityAuditor {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private boolean enabled;
    @OneToMany
    @ToString.Exclude
    private Set<Role> roles;
    private String username;
    private String ou;
    @OneToOne
    @JoinColumn(name = "app_id")
    private AllowedApp app;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
