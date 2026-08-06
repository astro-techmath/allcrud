package com.techmath.allcrud.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationUtilsTest {

    static class Probe {
        private String name;
        private Integer age;
        private String description;

        Probe(String name, Integer age, String description) {
            this.name = name;
            this.age = age;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public Integer getAge() {
            return age;
        }

        public String getDescription() {
            return description;
        }
    }

    @Test
    void givenObjectWithSomeNullProperties_whenGetNullPropertyNames_thenReturnsOnlyNullOnes() {
        Probe probe = new Probe("Alice", null, null);

        String[] nullProperties = ValidationUtils.getNullPropertyNames(probe);

        assertThat(nullProperties).containsExactlyInAnyOrder("age", "description");
    }

    @Test
    void givenObjectWithNoNullProperties_whenGetNullPropertyNames_thenReturnsEmptyArray() {
        Probe probe = new Probe("Alice", 30, "engineer");

        String[] nullProperties = ValidationUtils.getNullPropertyNames(probe);

        assertThat(nullProperties).isEmpty();
    }

    @Test
    void whenInstantiatedDirectly_thenThrowsIllegalAccessError() throws NoSuchMethodException {
        Constructor<ValidationUtils> constructor = ValidationUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .cause()
                .isInstanceOf(IllegalAccessError.class);
    }

}
