package com.techmath.allcrud.service;

import com.techmath.allcrud.common.UpdaterExample;
import com.techmath.allcrud.entity.AbstractEntity;
import com.techmath.allcrud.enums.CrudErrorMessage;
import com.techmath.allcrud.model.contract.SoftDeletable;
import com.techmath.allcrud.repository.EntityRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class CrudService<T extends AbstractEntity> {

    protected abstract EntityRepository<T> getRepository();

    @Transactional
    public T create(T entity) {
        if (!entity.isNew() && getRepository().existsById(Objects.requireNonNull(entity.getId()))) {
            throw new EntityExistsException();
        }
        entity.setId(null);
        return getRepository().save(entity);
    }

    public Optional<T> findById(Long id) {
        return getRepository().findById(id);
    }

    public List<T> findAll() {
        return getRepository().findAll();
    }

    public List<T> findAll(T filters) {
        var example = Example.of(Objects.requireNonNull(filters), defaultExampleMatcher());
        return getRepository().findAll(example);
    }

    public Page<T> findAll(T filters, Pageable pageable) {
        var example = Example.of(Objects.requireNonNull(filters), defaultExampleMatcher());
        return getRepository().findAll(example, pageable);
    }

    public List<T> findAllById(Iterable<Long> ids) {
        return getRepository().findAllById(ids);
    }

    @Transactional
    public T update(Long id, T toUpdate) {
        T entity = findById(id).orElseThrow(entityNotFoundExceptionSupplier(id));
        BeanUtils.copyProperties(toUpdate, entity, "id");
        return getRepository().save(entity);
    }

    @Transactional
    public T update(Long id, UpdaterExample<T> toUpdate) {
        T entity = findById(id).orElseThrow(entityNotFoundExceptionSupplier(id));
        BeanUtils.copyProperties(toUpdate.getProbe(), entity, toUpdate.getIgnoredPaths());
        return getRepository().save(entity);
    }

    @Transactional
    public void deleteById(Long id) {
        T entity = findById(id).orElseThrow(entityNotFoundExceptionSupplier(id));
        if (entity instanceof SoftDeletable) {
            softDelete(entity);
            getRepository().save(entity);
        } else {
            getRepository().deleteById(Objects.requireNonNull(entity.getId()));
        }
    }

    //*****************************************************************************************************************
    //******************************************* PRIVATE/PROTECTED METHODS *******************************************
    //*****************************************************************************************************************

    protected ExampleMatcher defaultExampleMatcher() {
        return ExampleMatcher.matchingAll().withIgnoreNullValues().withIgnoreCase();
    }

    protected Supplier<EntityNotFoundException> entityNotFoundExceptionSupplier(Long id) {
        var message = String.format(CrudErrorMessage.ENTITY_NOT_FOUND_MESSAGE.getMessage(), id);
        return () -> new EntityNotFoundException(message);
    }

    protected void softDelete(T entity) {
        throw new UnsupportedOperationException("Soft delete not implemented. Override `softDelete()` in your service to define how to handle it.");
    }

}
