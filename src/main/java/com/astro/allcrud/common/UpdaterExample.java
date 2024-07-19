package com.astro.allcrud.common;

import com.astro.allcrud.entity.AbstractEntity;
import com.astro.allcrud.util.ValidationUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class UpdaterExample<T extends AbstractEntity> implements Example<T> {

    private final T probe;
    private final String[] ignoredProperties;

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

    public String[] getIgnoredPaths() {
        return getMatcher().getIgnoredPaths().toArray(new String[0]);
    }

}