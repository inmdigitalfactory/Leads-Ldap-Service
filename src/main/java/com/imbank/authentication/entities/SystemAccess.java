package com.imbank.authentication.entities;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "user_roles")
@Builder
@AllArgsConstructor
public class SystemAccess extends EntityAuditor {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne
    @JoinColumn(name = "app_id")
    private AllowedApp app;
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER)
    @Builder.Default
    private Set<Role> roles = new java.util.LinkedHashSet<>();
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<AllowedApp> apps;//applicable to only ldap service

}
