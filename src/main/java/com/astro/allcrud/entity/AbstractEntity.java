package com.astro.allcrud.entity;

import org.springframework.data.domain.Persistable;

import java.util.Objects;

public interface AbstractEntity extends Persistable<Long> {

    void setId(Long id);

    @Override
    default boolean isNew() {
        return Objects.isNull(getId());
    }

}
