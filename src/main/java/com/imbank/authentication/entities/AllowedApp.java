package com.imbank.authentication.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.imbank.authentication.enums.AuthModule;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@Table(name = "allowed_apps", indexes = {
        @Index(name = "name", columnList = "name"),
        @Index(name = "accessToken", columnList = "access_token"),
        @Index(name = "enabled", columnList = "enabled"),
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @ElementCollection(fetch = FetchType.EAGER)
//    @ToString.Exclude
    private Set<AuthModule> modules = new HashSet<>(Arrays.asList(AuthModule.values()));

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AllowedApp that = (AllowedApp) o;
        return id != null && Objects.equals(id, that.id);
    }

}
