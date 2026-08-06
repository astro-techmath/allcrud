package com.techmath.allcrud.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DBConstUtilsTest {

    @Test
    void constantsMatchExpectedDbNamingConventions() {
        assertThat(DBConstUtils.AUD).isEqualTo("aud_");
        assertThat(DBConstUtils.ID).isEqualTo("_id");
        assertThat(DBConstUtils.FK).isEqualTo("fk_");
        assertThat(DBConstUtils.SEQUENCE).isEqualTo("_sequence");
        assertThat(DBConstUtils.TABLE).isEqualTo("_tb");
    }

    @Test
    void whenInstantiatedDirectly_thenThrowsIllegalAccessError() throws NoSuchMethodException {
        Constructor<DBConstUtils> constructor = DBConstUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .cause()
                .isInstanceOf(IllegalAccessError.class);
    }

}
