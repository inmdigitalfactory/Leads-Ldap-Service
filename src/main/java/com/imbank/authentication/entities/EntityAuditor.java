package com.imbank.authentication.entities;

import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public abstract class EntityAuditor {

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedOn;
    @CreatedBy
    private String createdBy;
    @LastModifiedBy
    private String modifiedBy;

}
