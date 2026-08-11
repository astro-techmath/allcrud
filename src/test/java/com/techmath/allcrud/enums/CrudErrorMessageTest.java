package com.techmath.allcrud.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrudErrorMessageTest {

    @Test
    void entityNotFoundMessage_hasExpectedTitleAndMessage() {
        assertThat(CrudErrorMessage.ENTITY_NOT_FOUND_MESSAGE.getTitle()).isEqualTo("Entity not found");
        assertThat(CrudErrorMessage.ENTITY_NOT_FOUND_MESSAGE.getMessage()).isEqualTo("Record with ID '%s' not found");
    }

    @Test
    void entityAlreadyExistsMessage_hasExpectedTitleAndMessage() {
        assertThat(CrudErrorMessage.ENTITY_ALREADY_EXISTS_MESSAGE.getTitle()).isEqualTo("Entity already exists");
        assertThat(CrudErrorMessage.ENTITY_ALREADY_EXISTS_MESSAGE.getMessage())
                .isEqualTo("Existent record with ID '%s' found");
    }

    @Test
    void validationConstraintsFailedMessage_hasExpectedTitleAndMessage() {
        assertThat(CrudErrorMessage.VALIDATION_CONSTRAINTS_FAILED_MESSAGE.getTitle())
                .isEqualTo("Field validation failed");
        assertThat(CrudErrorMessage.VALIDATION_CONSTRAINTS_FAILED_MESSAGE.getMessage())
                .isEqualTo("The field %s %s");
    }

}
