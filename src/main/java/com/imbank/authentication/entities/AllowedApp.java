package com.imbank.authentication.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "allowed_apps", indexes = {
        @Index(name = "name", columnList = "name"),
        @Index(name = "accessToken", columnList = "access_token"),
        @Index(name = "enabled", columnList = "enabled"),
})
public class AllowedApp extends EntityAuditor {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    @Column(name = "access_token")
    @ToString.Exclude
    @JsonIgnore
    private String accessToken;
    private boolean enabled;
    private long tokenValiditySeconds;
    private long refreshTokenValiditySeconds;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AllowedApp that = (AllowedApp) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
