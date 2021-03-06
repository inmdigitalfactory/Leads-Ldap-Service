package com.imbank.authentication.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
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
@NoArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "username", columnList = "username")
})
@AllArgsConstructor
@Builder(toBuilder = true)
public class User extends EntityAuditor {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private boolean enabled;
    private String department;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String description;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    @ToString.Exclude
    @JsonManagedReference
    private Set<SystemAccess> systemAccesses;
    private String username;
    private String baseDn;


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
