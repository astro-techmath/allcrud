package com.techmath.allcrud.entity;

import org.springframework.data.domain.Persistable;

import java.util.Objects;

/**
 * Represents a base contract for all entities used within the Allcrud framework.
 * <p>
 * Extends {@link Persistable} to provide a standardized structure for checking
 * whether an entity is new or existing, based on its {@code id}.
 * <p>
 * Entities implementing this interface must provide a {@code setId(ID id)} method
 * to support creation and update flows.
 *
 * <p>
 * The default implementation of {@link #isNew()} returns {@code true} if the {@code id} is {@code null},
 * which aligns with how Spring Data determines persistence state.
 *
 * @param <ID> the type of the entity's identifier
 *
 * @author Matheus Maia
 */
public interface AbstractEntity<ID> extends Persistable<ID> {

    /**
     * Sets the unique identifier of the entity.
     *
     * @param id the ID to assign
     */
    void setId(ID id);

    /**
     * Determines whether the entity is new (not yet persisted).
     * <p>
     * The default implementation returns {@code true} if the {@code id} is {@code null}.
     *
     * @return {@code true} if the entity has not been persisted yet, {@code false} otherwise
     */
    @Override
    default boolean isNew() {
        return Objects.isNull(getId());
    }

}
