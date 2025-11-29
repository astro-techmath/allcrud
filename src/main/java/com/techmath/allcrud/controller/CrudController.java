package com.techmath.allcrud.controller;

import com.techmath.allcrud.common.PageRequestVO;
import com.techmath.allcrud.common.UpdaterExample;
import com.techmath.allcrud.converter.Converter;
import com.techmath.allcrud.entity.AbstractEntity;
import com.techmath.allcrud.entity.AbstractEntityVO;
import com.techmath.allcrud.enums.CrudErrorMessage;
import com.techmath.allcrud.service.CrudService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.function.Supplier;

/**
 * Abstract controller that exposes reusable REST endpoints for CRUD operations.
 * <p>
 * This class provides out-of-the-box support for:
 * <ul>
 *   <li>Creating and updating entities via {@code POST}, {@code PUT}, {@code PATCH}</li>
 *   <li>Reading entities by ID and filtering with pagination</li>
 *   <li>Deleting entities with optional soft delete support via {@code DELETE}</li>
 * </ul>
 * <p>
 * To use it, extend this class in your controller and implement {@link #getService()} and {@link #getConverter()}.
 *
 * @param <T>  the type of entity. Must extend {@link AbstractEntity} and have an implementation of {@link Converter} to convert between value object and entity.
 * @param <VO> the type of value object. Must extend {@link AbstractEntityVO} and have an implementation of {@link Converter} to convert between entity and value object.
 *
 * @see CrudService
 * @see Converter
 * @see PageRequestVO
 * @see AbstractEntity
 * @see AbstractEntityVO
 *
 * @author Matheus Maia
 */
public abstract class CrudController<T extends AbstractEntity<ID>, VO extends AbstractEntityVO<ID>, ID> {

    /**
     * Returns the service implementation for the entity.
     *
     * @return the CRUD service
     */
    protected abstract CrudService<T, ID> getService();

    /**
     * Returns the converter between entity and value object.
     *
     * @return the converter
     */
    protected abstract Converter<T, VO, ID> getConverter();

    /**
     * Creates a new entity from the provided value object.
     *
     * @param vo the VO to create. Must be {@code @Valid}.
     * @return the created VO.
     *    <ul>
     *        <li>{@code 201 Created} if successful</li>
     *    </ul>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VO create(@Valid @RequestBody VO vo) {
        T entity = getConverter().convertToEntity(vo);
        T created = getService().create(entity);
        return getConverter().convertToVO(created);
    }

    /**
     * Finds an entity by its ID.
     *
     * @param id the entity ID.
     * @return the VO representation of the entity.
     *
     * @throws EntityNotFoundException if no entity with the given ID is found (HTTP 404).
     */
    @GetMapping(value = "/{id}")
    public VO findById(@PathVariable ID id) {
        T entity = getService().findById(id).orElseThrow(entityNotFoundExceptionSupplier(id));
        return getConverter().convertToVO(entity);
    }

    /**
     * Finds all entities matching the filter, with pagination support.
     *
     * @param filters   the VO filter fields.
     * @param pageable  pagination and sorting config.
     * @return a response entity containing:
     *         <ul>
     *             <li>{@code 204 No Content} if no records are found.</li>
     *             <li>{@code 206 Partial Content} if records are found.</li>
     *         </ul>
     */
    @GetMapping
    public ResponseEntity<List<VO>> findAll(VO filters, PageRequestVO pageable) {
        T entityFilters = getConverter().convertToEntity(filters);
        Page<T> result = getService().findAll(entityFilters, getPageableOf(pageable));
        HttpHeaders headers = mountPageableHttpHeaders(pageable, result);
        List<VO> content = result.map(getConverter()::convertToVO).getContent();
        HttpStatus status = result.getTotalElements() == 0 ? HttpStatus.NO_CONTENT : HttpStatus.PARTIAL_CONTENT;
        return new ResponseEntity<>(content, headers, status);
    }

    /**
     * Fully updates an entity by replacing all its fields.
     *
     * @param id the entity ID.
     * @param vo the VO with new values. Must be {@code @Valid}.
     * @return the updated VO.
     *    <ul>
     *        <li>{@code 200 OK} if successful.</li>
     *        <li>{@code 404 Not Found} if the entity does not exist.</li>
     *    </ul>
     *
     * @throws EntityNotFoundException if the entity does not exist (HTTP 404).
     */
    @PutMapping(value = "/{id}")
    public VO update(@PathVariable ID id, @Valid @RequestBody VO vo) {
        T toUpdate = getConverter().convertToEntity(vo);
        T updated = getService().update(id, toUpdate);
        return getConverter().convertToVO(updated);
    }

    /**
     * Partially updates an entity by applying only the non-null fields.
     *
     * @param id the entity ID
     * @param vo the VO with partial values
     * @return the updated VO
     *    <ul>
     *        <li>{@code 200 OK} if successful.</li>
     *        <li>{@code 404 Not Found} if the entity does not exist.</li>
     *    </ul>
     *
     * @throws EntityNotFoundException if the entity does not exist (HTTP 404).
     */
    @PatchMapping(value = "/{id}")
    public VO partialUpdate(@PathVariable ID id, @RequestBody VO vo) {
        T toUpdate = getConverter().convertToEntity(vo);
        T updated = getService().update(id, new UpdaterExample<>(toUpdate));
        return getConverter().convertToVO(updated);
    }

    /**
     * Deletes the entity by its ID.
     * <p>
     * Trigger status {@code 204 No Content} if successful, {@code 404 Not Found} if the entity does not exist.
     * </p>
     *
     * @param id the ID of the entity to delete.
     * @throws EntityNotFoundException if the entity does not exist (HTTP 404).
     */
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable ID id) {
        getService().deleteById(id);
    }

    //*****************************************************************************************************************
    //******************************************* PRIVATE/PROTECTED METHODS *******************************************
    //*****************************************************************************************************************

    /**
     * Creates a {@link Pageable} object from the provided VO.
     *
     * @param vo the request pagination data. Must not be null.
     * @return a {@link Pageable} instance.
     */
    protected Pageable getPageableOf(PageRequestVO vo) {
        return PageRequest.of(vo.getPage(), vo.getSize(), vo.getDirection(), vo.getOrderBy());
    }

    /**
     * Builds HTTP headers for pagination based on Spring Data {@link Page} results.
     *
     * @param pageable the pagination config. Must not be null.
     * @param result   the page result. Must not be null. Must contain at least one element. Must not contain null elements.
     * @return HTTP headers with pagination metadata.
     */
    protected HttpHeaders mountPageableHttpHeaders(PageRequestVO pageable, Page<T> result) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(PageRequestVO.CURRENT_PAGE_HEADER, String.valueOf(pageable.getPage()));
        headers.add(PageRequestVO.CURRENT_ELEMENTS_HEADER, String.valueOf(result.getNumberOfElements()));
        headers.add(PageRequestVO.TOTAL_ELEMENTS_HEADER, String.valueOf(result.getTotalElements()));
        headers.add(PageRequestVO.TOTAL_PAGES_HEADER, String.valueOf(result.getTotalPages()));
        return headers;
    }

    /**
     * Returns a supplier for {@link EntityNotFoundException} with a formatted message.
     *
     * @param id the ID that was not found.
     * @return a supplier that throws the exception.
     */
    protected Supplier<EntityNotFoundException> entityNotFoundExceptionSupplier(ID id) {
        var message = String.format(CrudErrorMessage.ENTITY_NOT_FOUND_MESSAGE.getMessage(), id);
        return () -> new EntityNotFoundException(message);
    }

}
