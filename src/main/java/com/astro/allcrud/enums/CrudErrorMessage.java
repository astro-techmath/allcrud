package com.astro.allcrud.enums;

import lombok.Getter;

@Getter
public enum CrudErrorMessage {

    ENTITY_NOT_FOUND_MESSAGE("Entity not found", "Record with ID '%d' not found"),
    ENTITY_ALREADY_EXISTS_MESSAGE("Entity already exists", "Existent record with ID '%d' found"),
    VALIDATION_CONSTRAINTS_FAILED_MESSAGE("Field validation failed", "The field %s %s");

    private final String title;
    private final String message;

    CrudErrorMessage(String title, String message) {
        this.title = title;
        this.message = message;
    }

}
