package com.techmath.allcrud.common;

import com.techmath.allcrud.entity.AbstractEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdaterExampleTest {

    static class ProbeEntity implements AbstractEntity<Long> {
        private Long id;
        private String name;
        private String description;

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    @Test
    void givenProbeWithSomeNullFields_thenIgnoredPathsIncludeThemAndId() {
        ProbeEntity probe = new ProbeEntity();
        probe.setId(1L);
        probe.setName("Widget");
        probe.setDescription(null);

        UpdaterExample<ProbeEntity, Long> example = new UpdaterExample<>(probe);

        assertThat(example.getProbe()).isSameAs(probe);
        assertThat(example.getIgnoredPaths()).contains("description", "id");
        assertThat(example.getIgnoredPaths()).doesNotContain("name");
    }

    @Test
    void givenExplicitIgnoredProperties_thenMatcherUsesThemPlusId() {
        ProbeEntity probe = new ProbeEntity();
        probe.setId(1L);
        probe.setName("Widget");

        UpdaterExample<ProbeEntity, Long> example = new UpdaterExample<>(probe, new String[]{"name"});

        assertThat(example.getIgnoredPaths()).containsExactlyInAnyOrder("name", "id");
    }

}
