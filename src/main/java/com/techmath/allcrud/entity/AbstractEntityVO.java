package com.techmath.allcrud.entity;

import java.io.Serializable;

/**
 * Base interface for all Value Objects (VOs) used in the Allcrud framework.
 * <p>
 * VOs represent data transfer models used in controllers and external communication,
 * and are typically mapped to/from JPA entities via a {@code Converter}.
 * <p>
 * Every VO must expose an {@code id} field for consistency with the entity layer.
 *
 * <p>
 * Extends {@link Serializable} to support transmission in distributed environments.
 *
 * @author Matheus Maia
 */
public interface AbstractEntityVO extends Serializable {

    /**
     * Returns the identifier of the VO.
     *
     * @return the VO's ID
     */
    Long getId();

    /**
     * Sets the identifier of the VO.
     *
     * @param id the ID to assign
     */
    void setId(Long id);

}
