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

/**
 * Abstract generic service that encapsulates reusable CRUD logic for JPA entities.
 * <p>
 * This class serves as the foundation for concrete service classes in the Allcrud framework.
 * It provides support for:
 * <ul>
 *     <li>Entity creation and update (full and partial)</li>
 *     <li>Find by ID, list all, filtered queries, and pagination</li>
 *     <li>Soft delete behavior via {@link SoftDeletable}</li>
 * </ul>
 *
 * <p>
 * To use this service, extend it with your entity type and override {@link #getRepository()}.
 * Optionally, override {@link #softDelete(AbstractEntity)} to enable soft deletion logic.
 *
 * @param <T> the type of the entity. Must extend {@link AbstractEntity} and implement {@link SoftDeletable} if soft delete is desired.
 *
 * @see com.techmath.allcrud.repository.EntityRepository
 * @see com.techmath.allcrud.common.UpdaterExample
 * @see com.techmath.allcrud.model.contract.SoftDeletable
 *
 * @author Matheus Maia
 */
public abstract class CrudService<T extends AbstractEntity<ID>, ID> {

    /**
     * Provides the repository implementation for the entity.
     *
     * @return the repository instance
     */
    protected abstract EntityRepository<T, ID> getRepository();

    /**
     * Creates a new entity instance.
     * Throws {@link EntityExistsException} if the entity already exists.
     *
     * @param entity the entity to create. Must not be null.
     * @return the saved entity.
     */
    @Transactional
    public T create(T entity) {
        if (!entity.isNew() && getRepository().existsById(Objects.requireNonNull(entity.getId()))) {
            throw new EntityExistsException();
        }
        entity.setId(null);
        return getRepository().save(entity);
    }

    /**
     * Finds an entity by its ID.
     *
     * @param id the entity ID. Must not be null.
     * @return an {@link Optional} with the entity if found.
     */
    public Optional<T> findById(ID id) {
        return getRepository().findById(id);
    }

    /**
     * Returns all entities in the repository.
     *
     * @return list of all entities.
     */
    public List<T> findAll() {
        return getRepository().findAll();
    }

    /**
     * Returns all entities matching the provided filter.
     *
     * @param filters the entity with filter values. Must not be null.
     * @return filtered list of entities.
     */
    public List<T> findAll(T filters) {
        var example = Example.of(Objects.requireNonNull(filters), defaultExampleMatcher());
        return getRepository().findAll(example);
    }

    /**
     * Returns a paginated list of entities matching the provided filter.
     *
     * @param filters   the entity with filter values. Must not be null.
     * @param pageable  pagination information. Must not be null.
     * @return page of filtered entities.
     */
    public Page<T> findAll(T filters, Pageable pageable) {
        var example = Example.of(Objects.requireNonNull(filters), defaultExampleMatcher());
        return getRepository().findAll(example, pageable);
    }

    /**
     * Finds all entities by a list of IDs.
     *
     * @param ids the IDs to search. Must not be null nor contain any null values.
     * @return list of matching entities.
     */
    public List<T> findAllById(Iterable<ID> ids) {
        return getRepository().findAllById(ids);
    }

    /**
     * Updates an existing entity by replacing all its fields (except ID).
     *
     * @param id       the entity ID. Must not be null.
     * @param toUpdate the new data. Must not be null. All fields except ID will be replaced.
     * @return the updated entity.
     */
    @Transactional
    public T update(ID id, T toUpdate) {
        T entity = findById(id).orElseThrow(entityNotFoundExceptionSupplier(id));
        BeanUtils.copyProperties(toUpdate, entity, "id");
        return getRepository().save(entity);
    }

    /**
     * Partially updates an existing entity using a probe and ignored fields.
     *
     * @param id       the entity ID. Must not be null.
     * @param toUpdate the probe object wrapped in {@link UpdaterExample}. Must not be null.
     * @return the updated entity.
     */
    @Transactional
    public T update(ID id, UpdaterExample<T, ID> toUpdate) {
        T entity = findById(id).orElseThrow(entityNotFoundExceptionSupplier(id));
        BeanUtils.copyProperties(toUpdate.getProbe(), entity, toUpdate.getIgnoredPaths());
        return getRepository().save(entity);
    }

    /**
     * Deletes the entity by ID.
     * <p>
     * If the entity implements {@link SoftDeletable}, the soft delete logic will be applied instead.
     *
     * @param id the ID of the entity to delete. Must not be null.
     */
    @Transactional
    public void deleteById(ID id) {
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

    /**
     * Builds a default {@link ExampleMatcher} with case-insensitive and null-ignoring behavior.
     *
     * @return the matcher instance.
     */
    protected ExampleMatcher defaultExampleMatcher() {
        return ExampleMatcher.matchingAll().withIgnoreNullValues().withIgnoreCase();
    }

    /**
     * Returns a supplier that throws {@link EntityNotFoundException} with a formatted message.
     *
     * @param id the ID of the entity that was not found. Must not be null.
     * @return a supplier of exception.
     */
    protected Supplier<EntityNotFoundException> entityNotFoundExceptionSupplier(ID id) {
        var message = String.format(CrudErrorMessage.ENTITY_NOT_FOUND_MESSAGE.getMessage(), id);
        return () -> new EntityNotFoundException(message);
    }

    /**
     * Override this method in your service to apply soft delete logic.
     * See {@link SoftDeletable}.
     *
     * @param entity the entity to be soft-deleted. Must not be null.
     */
    protected void softDelete(T entity) {
        throw new UnsupportedOperationException("Soft delete not implemented. Override `softDelete()` in your service to define how to handle it.");
    }

}
