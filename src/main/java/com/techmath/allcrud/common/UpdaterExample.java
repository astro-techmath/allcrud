package com.techmath.allcrud.common;

import com.techmath.allcrud.entity.AbstractEntity;
import com.techmath.allcrud.util.ValidationUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom {@link Example} implementation used for update operations.
 * <p>
 * Ignores all {@code null} fields in the probe object to ensure only non-null
 * values are considered during matching. Also forces the {@code id} field to be ignored.
 * <p>
 * Commonly used to build queries for partial updates where only set fields matter.
 *
 * @param <T> the type of entity being updated
 *
 * @author Matheus Maia
 */
@AllArgsConstructor
public class UpdaterExample<T extends AbstractEntity> implements Example<T> {

    private final T probe;
    private final String[] ignoredProperties;

    /**
     * Constructs an {@code UpdaterExample} by automatically determining null properties to ignore.
     *
     * @param probe the entity containing values to be matched
     */
    public UpdaterExample(T probe) {
        this.probe = probe;
        this.ignoredProperties = ValidationUtils.getNullPropertyNames(probe);
    }

    @Override
    public T getProbe() {
        return probe;
    }

    @Override
    public ExampleMatcher getMatcher() {
        var ignoredPaths = new ArrayList<>(List.of(this.ignoredProperties));
        ignoredPaths.add("id");
        return ExampleMatcher.matchingAll().withIgnorePaths(ignoredPaths.toArray(new String[0]));
    }

    /**
     * Returns the list of ignored paths used in the example matcher.
     *
     * @return an array of ignored property names
     */
    public String[] getIgnoredPaths() {
        return getMatcher().getIgnoredPaths().toArray(new String[0]);
    }

}