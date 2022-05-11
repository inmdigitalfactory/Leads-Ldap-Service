package com.imbank.authentication.entities;

import com.imbank.authentication.enums.AuditAction;
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
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "audit_logs")
@Builder
public class AuditLog extends EntityAuditor {
    @Id
    @GeneratedValue
    private Long id;
    @Enumerated(EnumType.STRING)
    private AuditAction action;
    private String appName;//the app this action was performed on
    private String userName;//the user this action was performed on
    private long appId;
    private long userId;
    private String metadata;
    private String role;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AuditLog that = (AuditLog) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
