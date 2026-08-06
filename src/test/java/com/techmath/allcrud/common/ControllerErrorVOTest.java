package com.techmath.allcrud.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerErrorVOTest {

    @Test
    void givenErrorAndDescription_thenAccessorsReturnThem() {
        ControllerErrorVO error = new ControllerErrorVO("NOT_FOUND", "Entity with id 1 not found");

        assertThat(error.error()).isEqualTo("NOT_FOUND");
        assertThat(error.description()).isEqualTo("Entity with id 1 not found");
    }

    @Test
    void givenSameValues_thenRecordsAreEqual() {
        ControllerErrorVO first = new ControllerErrorVO("BAD_REQUEST", "Invalid field");
        ControllerErrorVO second = new ControllerErrorVO("BAD_REQUEST", "Invalid field");

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    }

}
