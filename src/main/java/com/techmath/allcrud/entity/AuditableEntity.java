package com.techmath.allcrud.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Abstract base class for entities that require auditing metadata.
 * <p>
 * Extends {@link AbstractEntity} and provides automatic population of
 * creation and modification timestamps and users using Spring Data JPA auditing.
 * <p>
 * This class is annotated with {@link MappedSuperclass} and must be extended by JPA entities
 * that need audit tracking for lifecycle events.
 * <p>
 * The auditing information is populated automatically via {@link AuditingEntityListener}.
 *
 * @author Matheus Maia
 */
@Getter @Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity<ID> implements AbstractEntity<ID> {

    /**
     * Timestamp when the entity was created.
     * Populated automatically and cannot be updated.
     */
    @CreatedDate
    @Column(name = "aud_created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    /**
     * Identifier of the user who created the entity.
     * Populated automatically and cannot be updated.
     */
    @CreatedBy
    @Column(name = "aud_created_by", nullable = false, updatable = false)
    private Long createdBy;

    /**
     * Timestamp of the last update to the entity.
     * Populated automatically on update operations.
     */
    @LastModifiedDate
    @Column(name = "aud_last_updated_date", insertable = false)
    private LocalDateTime lastUpdatedDate;

    /**
     * Identifier of the user who last modified the entity.
     * Populated automatically on update operations.
     */
    @LastModifiedBy
    @Column(name = "aud_last_updated_by", insertable = false)
    private LocalDateTime lastUpdatedBy;

}
