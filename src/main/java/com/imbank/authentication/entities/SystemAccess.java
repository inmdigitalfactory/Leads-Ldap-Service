package com.imbank.authentication.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "system_accesses")
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
    @OneToOne
    @JsonBackReference
    @JoinColumn(name = "user_id")
    private User user;
    @OneToOne
    @JoinColumn(name = "role_id")
    private Role role;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<AllowedApp> apps;//applicable to only ldap service

}
