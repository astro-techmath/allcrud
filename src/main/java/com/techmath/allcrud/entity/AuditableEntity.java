package com.techmath.allcrud.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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
 * @param <ID> the type of the entity's identifier
 *
 * @author Matheus Maia
 */
@Getter @Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity<ID> implements AbstractEntity<ID> {

    /**
     * Protected constructor to prevent direct instantiation.
     * This class is designed to be extended by JPA entities.
     */
    protected AuditableEntity() {
        // Constructor for JPA and subclasses
    }

    /**
     * Timestamp when the entity was created.
     * Populated automatically and cannot be updated.
     */
    @CreatedDate
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "aud_created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    /**
     * Identifier of the user who created the entity.
     * Populated automatically and cannot be updated.
     */
    @CreatedBy
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "aud_created_by", nullable = false, updatable = false)
    private ID createdBy;

    /**
     * Timestamp of the last update to the entity.
     * Populated automatically on update operations.
     */
    @LastModifiedDate
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "aud_last_updated_date", insertable = false)
    private LocalDateTime lastUpdatedDate;

    /**
     * Identifier of the user who last modified the entity.
     * Populated automatically on update operations.
     */
    @LastModifiedBy
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "aud_last_updated_by", insertable = false)
    private ID lastUpdatedBy;

}
