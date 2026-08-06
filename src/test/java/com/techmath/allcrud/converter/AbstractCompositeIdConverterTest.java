package com.techmath.allcrud.converter;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// AbstractCompositeIdConverter is abstract but has real, self-contained logic (Base64URL decode +
// JSON deserialize) with no dependency on Spring context or a consumer's entity - a minimal
// concrete subclass here (no overrides, just satisfies the protected constructor) is the standard
// way to unit test a template-method abstract class, not a facade test. Unlike CrudController/
// CrudService/AbstractGlobalExceptionHandler (excluded - see build.gradle.kts), this class's
// entire behavior is exercised without any framework wiring.
class AbstractCompositeIdConverterTest {

    record CompositeId(String tenant, Long localId) {
    }

    static class TestCompositeIdConverter extends AbstractCompositeIdConverter<CompositeId> {
        TestCompositeIdConverter(ObjectMapper objectMapper) {
            super(CompositeId.class, objectMapper);
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TestCompositeIdConverter converter = new TestCompositeIdConverter(objectMapper);

    @Test
    void givenValidBase64UrlEncodedJson_whenConvert_thenReturnsDeserializedObject() {
        CompositeId original = new CompositeId("acme", 42L);
        String encoded = Base64.getUrlEncoder().encodeToString(objectMapper.writeValueAsBytes(original));

        CompositeId result = converter.convert(encoded);

        assertThat(result).isEqualTo(original);
    }

    @Test
    void givenBlankSource_whenConvert_thenReturnsNull() {
        assertThat(converter.convert("")).isNull();
        assertThat(converter.convert("   ")).isNull();
        assertThat(converter.convert(null)).isNull();
    }

    @Test
    void givenInvalidBase64_whenConvert_thenThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> converter.convert("not-valid-base64url-json!!!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid ID format");
    }

    @Test
    void givenValidBase64ButInvalidJson_whenConvert_thenThrowsIllegalArgumentException() {
        String encoded = Base64.getUrlEncoder().encodeToString("not json".getBytes());

        assertThatThrownBy(() -> converter.convert(encoded))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid ID format");
    }

}
