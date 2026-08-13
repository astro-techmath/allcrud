package com.techmath.allcrud.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// Regression check for the jackson-datatype-jsr310 removal (see build.gradle.kts) - confirms
// Boot's own auto-configured ObjectMapper (Jackson 3, real context via @JsonTest, not a
// hand-rolled one) still round-trips AuditableEntity's LocalDateTime fields correctly without
// that explicit Jackson 2 module. Jackson 3's databind has java.time support built in, but that
// claim is verified here rather than trusted from release notes alone.
@JsonTest
class AuditableEntityJacksonTest {

    @SpringBootApplication
    static class TestApplication {
    }

    static class TestAuditableEntity extends AuditableEntity<Long> {
        private Long id;

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public void setId(Long id) {
            this.id = id;
        }
    }

    @Autowired
    private JacksonTester<TestAuditableEntity> json;

    @Autowired
    private JacksonTester<LocalDateTime> rawLocalDateTimeJson;

    @Test
    void givenAuditableEntity_whenSerialized_thenLocalDateTimeFieldsAreWrittenAsIso() throws Exception {
        TestAuditableEntity entity = new TestAuditableEntity();
        entity.setId(1L);
        entity.setCreatedDate(LocalDateTime.of(2026, 8, 6, 10, 30, 0));
        entity.setLastUpdatedDate(LocalDateTime.of(2026, 8, 6, 11, 45, 0));

        String content = json.write(entity).getJson();

        assertThat(content).contains(
                "\"createdDate\":\"2026-08-06T10:30:00\"",
                "\"lastUpdatedDate\":\"2026-08-06T11:45:00\"");
    }

    // createdDate/lastUpdatedDate are @JsonProperty(access = READ_ONLY) - by design, client-
    // supplied values for them are ignored on deserialization (only @CreatedDate/@LastModifiedDate
    // JPA auditing populates them). This isn't something jackson-datatype-jsr310's removal could
    // regress - it's the annotation's own contract, confirmed here rather than assumed.
    @Test
    void givenJsonWithLocalDateTimeFields_whenDeserialized_thenReadOnlyFieldsStayNull() throws Exception {
        String content = """
                {
                  "id": 1,
                  "createdDate": "2026-08-06T10:30:00",
                  "lastUpdatedDate": "2026-08-06T11:45:00"
                }
                """;

        TestAuditableEntity entity = json.parseObject(content);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getCreatedDate()).isNull();
        assertThat(entity.getLastUpdatedDate()).isNull();
    }

    // The actual jackson-datatype-jsr310 removal regression check: proves Jackson 3's databind
    // round-trips java.time.LocalDateTime on its own, independent of AuditableEntity's READ_ONLY
    // gate above - this is the capability that module used to provide explicitly.
    @Test
    void givenLocalDateTime_whenRoundTripped_thenValueIsPreserved() throws Exception {
        LocalDateTime original = LocalDateTime.of(2026, 8, 6, 10, 30, 0);

        String content = rawLocalDateTimeJson.write(original).getJson();
        LocalDateTime roundTripped = rawLocalDateTimeJson.parseObject(content);

        assertThat(content).isEqualTo("\"2026-08-06T10:30:00\"");
        assertThat(roundTripped).isEqualTo(original);
    }

}
