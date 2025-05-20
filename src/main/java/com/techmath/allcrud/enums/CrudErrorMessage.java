package com.techmath.allcrud.enums;

import lombok.Getter;

/**
 * Enum representing standard error messages used in CRUD operations.
 * <p>
 * Each entry contains a short title and a message template with placeholders
 * that can be formatted at runtime to produce consistent API error responses.
 *
 * <p>Example usage:
 * <pre>{@code
 * throw new BusinessException(CrudErrorMessage.ENTITY_NOT_FOUND_MESSAGE, id);
 * }</pre>
 *
 * @see com.techmath.allcrud.exception.BusinessException
 * @see com.techmath.allcrud.common.ControllerErrorVO
 *
 * @author Matheus Maia
 */
@Getter
public enum CrudErrorMessage {

    /** Error indicating that the requested entity was not found. */
    ENTITY_NOT_FOUND_MESSAGE("Entity not found", "Record with ID '%d' not found"),

    /** Error indicating that the entity already exists in the database. */
    ENTITY_ALREADY_EXISTS_MESSAGE("Entity already exists", "Existent record with ID '%d' found"),

    /** Error indicating that a field failed validation. */
    VALIDATION_CONSTRAINTS_FAILED_MESSAGE("Field validation failed", "The field %s %s");

    private final String title;
    private final String message;

    CrudErrorMessage(String title, String message) {
        this.title = title;
        this.message = message;
    }

}
