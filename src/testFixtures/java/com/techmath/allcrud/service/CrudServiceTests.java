package com.techmath.allcrud.service;

import com.techmath.allcrud.common.UpdaterExample;
import com.techmath.allcrud.entity.AbstractEntity;
import com.techmath.allcrud.repository.EntityRepository;
import com.techmath.allcrud.util.ValidationUtils;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public abstract class CrudServiceTests<T extends AbstractEntity> {

    private final Class<T> entityClass;

    protected abstract EntityRepository<T> getRepository();

    protected abstract CrudService<T> getService();

    protected CrudServiceTests(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Test
    public void givenEntity_whenCreate_thenReturnCreatedEntity() {
        T entityCreated = Instancio.create(entityClass);
        T entityToCreate = Instancio.create(entityClass);
        BeanUtils.copyProperties(entityCreated, entityToCreate);
        entityToCreate.setId(null);

        when(getRepository().save(any())).thenReturn(entityCreated);

        T actual = assertDoesNotThrow(() -> getService().create(entityToCreate));

        assertNotNull(actual);
        assertEquals(entityCreated.toString(), actual.toString());
    }

    @Test
    public void givenEntityWithId_whenCreate_thenThrowEntityExistsException() {
        T entityCreated = Instancio.create(entityClass);
        when(getRepository().existsById(anyLong())).thenReturn(true);
        CrudService<T> service = getService();
        assertThrows(EntityExistsException.class, () -> service.create(entityCreated));
        verify(getRepository(), never()).save(any());
    }

    @Test
    public void givenNotNewNonExistentEntity_whenCreate_thenReturnCreatedEntity() {
        T entityCreated = Instancio.create(entityClass);
        T entityToCreate = Instancio.create(entityClass);
        BeanUtils.copyProperties(entityCreated, entityToCreate);

        when(getRepository().existsById(anyLong())).thenReturn(false);
        when(getRepository().save(any())).thenReturn(entityCreated);

        entityToCreate.setId(100L);
        T actual = assertDoesNotThrow(() -> getService().create(entityToCreate));

        assertNotNull(actual);
        assertEquals(entityCreated.toString(), actual.toString());
    }

    @Test
    public void givenEntity_whenFindById_thenReturnEntity() {
        T entityCreated = Instancio.create(entityClass);

        when(getRepository().findById(anyLong())).thenReturn(Optional.ofNullable(entityCreated));

        Optional<T> actual = getService().findById(1L);

        assertTrue(actual.isPresent());
        assertNotNull(entityCreated);
        assertEquals(actual.get().toString(), entityCreated.toString());
    }

    @Test
    public void givenEntity_whenFindById_thenReturnEmpty() {
        when(getRepository().findById(anyLong())).thenReturn(Optional.empty());

        Optional<T> actual = getService().findById(2L);

        assertFalse(actual.isPresent());
    }

    @Test
    public void givenEntity_whenFindAllWithNoFilters_thenReturnPagedAllEntities() {
        T entity1 = Instancio.create(entityClass);
        T entity2 = Instancio.create(entityClass);
        T emptyObject = Instancio.createBlank(entityClass);

        Page<T> expectedPage1 = new PageImpl<>(List.of(entity1));
        Pageable page1 = PageRequest.of(0, 1);
        Page<T> expectedPage2 = new PageImpl<>(List.of(entity2));
        Pageable page2 = PageRequest.of(1, 1);

        when(getRepository().findAll(any(Example.class), eq(page1))).thenReturn(expectedPage1);
        when(getRepository().findAll(any(Example.class), eq(page2))).thenReturn(expectedPage2);

        Page<T> actual1 = getService().findAll(emptyObject, page1);
        Page<T> actual2 = getService().findAll(emptyObject, page2);

        assertFalse(actual1.isEmpty());
        assertFalse(actual2.isEmpty());
        assertEquals(1, actual1.getContent().size());
        assertEquals(1, actual2.getContent().size());
        assertArrayEquals(expectedPage1.getContent().toArray(), actual1.getContent().toArray());
        assertArrayEquals(expectedPage2.getContent().toArray(), actual2.getContent().toArray());
    }

    @Test
    public void givenEntity_whenFindAllWithAnyFilters_thenReturnPagedAllFilteredEntities() {
        T entity1 = Instancio.create(entityClass);
        T entity2 = Instancio.create(entityClass);
        T filterEntity1 = Instancio.create(entityClass);
        T filterEntity2 = Instancio.create(entityClass);
        BeanUtils.copyProperties(entity1, filterEntity1);
        BeanUtils.copyProperties(entity2, filterEntity2);

        Page<T> expectedPage1 = new PageImpl<>(List.of(entity1));
        Pageable page1 = PageRequest.of(0, 1);
        Page<T> expectedPage2 = new PageImpl<>(List.of(entity2));
        Pageable page2 = PageRequest.of(1, 1);

        when(getRepository().findAll(any(Example.class), eq(page1))).thenReturn(expectedPage1);
        when(getRepository().findAll(any(Example.class), eq(page2))).thenReturn(expectedPage2);

        Page<T> actual1 = getService().findAll(filterEntity1, page1);
        Page<T> actual2 = getService().findAll(filterEntity2, page2);

        assertFalse(actual1.isEmpty());
        assertFalse(actual2.isEmpty());
        assertEquals(1, actual1.getContent().size());
        assertEquals(1, actual2.getContent().size());
        assertArrayEquals(expectedPage1.getContent().toArray(), actual1.getContent().toArray());
        assertArrayEquals(expectedPage2.getContent().toArray(), actual2.getContent().toArray());
    }

    @Test
    public void givenEntity_whenFindAllWithNotFoundFilters_thenReturnPagedEmpty() {
        T filterToNotFound = Instancio.create(entityClass);
        when(getRepository().findAll(any(Example.class), any(Pageable.class))).thenReturn(Page.empty());

        Page<T> actual = getService().findAll(filterToNotFound, Pageable.unpaged());

        assertTrue(actual.isEmpty());
    }

    @Test
    public void givenEntity_whenUpdate_thenReturnUpdatedEntity() {
        T entityCreated = Instancio.create(entityClass);
        T entityUpdated = Instancio.create(entityClass);

        when(getRepository().findById(anyLong())).thenReturn(Optional.ofNullable(entityCreated));
        when(getRepository().save(any())).thenReturn(entityUpdated);

        T actual = assertDoesNotThrow(() -> getService().update(1L, entityUpdated));
        assertNotNull(actual);
        assertEquals(entityUpdated.toString(), actual.toString());
    }

    @Test
    public void givenEntity_whenUpdate_thenThrowEntityNotFoundException() {
        T toUpdate = Instancio.create(entityClass);

        when(getRepository().findById(0L)).thenReturn(Optional.empty());
        CrudService<T> service = getService();
        assertThrows(EntityNotFoundException.class, () -> service.update(0L, toUpdate));
        verify(getRepository(), never()).save(any());
    }

    @Test
    public void givenEntity_whenPartialUpdate_thenReturnUpdatedEntity() {
        T entityCreated = Instancio.create(entityClass);
        T partialUpdated = Instancio.create(entityClass);

        when(getService().findById(anyLong())).thenReturn(Optional.of(entityCreated));
        when(getRepository().save(any())).thenReturn(partialUpdated);

        String[] ignoredProperties = ValidationUtils.getNullPropertyNames(partialUpdated);
        T actual = assertDoesNotThrow(() -> getService().update(1L, new UpdaterExample<>(partialUpdated, ignoredProperties)));

        assertNotNull(actual);
        assertEquals(actual.toString(), partialUpdated.toString());
    }

    @Test
    public void givenEntity_whenPartialUpdate_theThrowEntityNotFoundException() {
        T toPartialUpdate = Instancio.create(entityClass);

        when(getService().findById(anyLong())).thenReturn(Optional.empty());

        String[] ignoredProperties = ValidationUtils.getNullPropertyNames(toPartialUpdate);
        CrudService<T> service = getService();
        UpdaterExample<T> example = new UpdaterExample<>(toPartialUpdate, ignoredProperties);

        assertThrows(EntityNotFoundException.class, () -> service.update(0L, example));
        verify(getRepository(), never()).save(any());
    }

    @Test
    public void givenEntity_whenDelete_thenDeleteEntity() {
        T entityCreated = Instancio.create(entityClass);

        doNothing().when(getRepository()).deleteById(anyLong());
        when(getRepository().findById(anyLong())).thenReturn(Optional.ofNullable(entityCreated));
        assertDoesNotThrow(() -> getService().deleteById(1L));
    }

    @Test
    public void givenEntity_whenDeleteById_thenThrowEntityNotFoundException() {
        when(getRepository().findById(anyLong())).thenReturn(Optional.empty());
        CrudService<T> service = getService();
        assertThrows(EntityNotFoundException.class, () -> service.deleteById(0L));
        verify(getRepository(), never()).deleteById(anyLong());
    }

}
