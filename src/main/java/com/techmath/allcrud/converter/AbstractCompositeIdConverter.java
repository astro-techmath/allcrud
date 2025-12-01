package com.techmath.allcrud.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Base64;

/**
 * Base generic converter for Composite IDs using Base64 encoded JSON.
 * This handles ANY combination of types (UUID, String, Long) automatically.
 *
 * @param <T> The Composite Key Type
 */
public abstract class AbstractCompositeIdConverter<T> implements Converter<String, T> {

    private final Class<T> idType;
    private final ObjectMapper objectMapper;

    protected AbstractCompositeIdConverter(Class<T> idType, ObjectMapper objectMapper) {
        this.idType = idType;
        this.objectMapper = objectMapper;
    }

    @Override
    public T convert(String source) {
        if (!StringUtils.hasText(source)) {
            return null;
        }

        try {
            // 1. Decode Base64Url -> JSON String
            byte[] decodedBytes = Base64.getUrlDecoder().decode(source);

            // 2. Deserialize JSON -> Object
            return objectMapper.readValue(decodedBytes, idType);
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ID format. Expected Base64URL encoded JSON.", e);
        }
    }

}
