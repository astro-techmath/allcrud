package com.astro.allcrud.service;

import com.astro.allcrud.common.UpdaterExample;
import com.astro.allcrud.entity.AbstractEntity;
import com.astro.allcrud.repository.EntityRepository;
import com.astro.allcrud.util.ValidationUtils;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public abstract class CrudServiceTests<T extends AbstractEntity> {

    protected abstract EntityRepository<T> getRepository();

    protected abstract CrudService<T> getService();

    protected T entityToCreate;
    protected T entityCreated;
    protected T entityUpdated;
    protected T emptyObject;
    protected T filterToNotFound;
    protected T filterEntity1;
    protected T filterEntity2;
    protected T toUpdate;
    protected T toPartialUpdate;
    protected T partialUpdated;

    @Test
    public void givenEntity_whenCreate_thenReturnCreatedEntity() {
        when(getRepository().save(any())).thenReturn(entityCreated);

        T actual = assertDoesNotThrow(() -> getService().create(entityToCreate));

        assertNotNull(actual);
        assertEquals(entityCreated.toString(), actual.toString());
    }

    @Test
    public void givenEntityWithId_whenCreate_thenThrowEntityExistsException() {
        when(getRepository().existsById(anyLong())).thenReturn(true);
        CrudService<T> service = getService();
        assertThrows(EntityExistsException.class, () -> service.create(entityCreated));
        verify(getRepository(), never()).save(any());
    }

    @Test
    public void givenNotNewNonExistentEntity_whenCreate_thenReturnCreatedEntity() {
        when(getRepository().existsById(anyLong())).thenReturn(false);
        when(getRepository().save(any())).thenReturn(entityCreated);

        entityToCreate.setId(100L);
        T actual = assertDoesNotThrow(() -> getService().create(entityToCreate));

        assertNotNull(actual);
        assertEquals(entityCreated.toString(), actual.toString());
    }

    @Test
    public void givenEntity_whenFindById_thenReturnEntity() {
        when(getRepository().findById(anyLong())).thenReturn(Optional.ofNullable(entityCreated));

        Optional<T> actual = getService().findById(1L);

        assertTrue(actual.isPresent());
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
        entityUpdated.setId(2L);

        Page<T> expectedPage1 = new PageImpl<>(List.of(entityCreated));
        Pageable page1 = PageRequest.of(0, 1);
        Page<T> expectedPage2 = new PageImpl<>(List.of(entityUpdated));
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
        entityUpdated.setId(2L);

        Page<T> expectedPage1 = new PageImpl<>(List.of(entityCreated));
        Pageable page1 = PageRequest.of(0, 1);
        Page<T> expectedPage2 = new PageImpl<>(List.of(entityUpdated));
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
        when(getRepository().findAll(any(Example.class), any(Pageable.class))).thenReturn(Page.empty());

        Page<T> actual = getService().findAll(filterToNotFound, Pageable.unpaged());

        assertTrue(actual.isEmpty());
    }

    @Test
    public void givenEntity_whenUpdate_thenReturnUpdatedEntity() {
        when(getRepository().findById(anyLong())).thenReturn(Optional.ofNullable(entityCreated));
        when(getRepository().save(any())).thenReturn(entityUpdated);

        T actual = assertDoesNotThrow(() -> getService().update(entityCreated.getId(), toUpdate));
        assertNotNull(actual);
        assertEquals(entityUpdated.toString(), actual.toString());
    }

    @Test
    public void givenEntity_whenUpdate_thenThrowEntityNotFoundException() {
        when(getRepository().findById(0L)).thenReturn(Optional.empty());
        CrudService<T> service = getService();
        assertThrows(EntityNotFoundException.class, () -> service.update(0L, toUpdate));
        verify(getRepository(), never()).save(any());
    }

    @Test
    public void givenEntity_whenPartialUpdate_thenReturnUpdatedEntity() {
        when(getService().findById(anyLong())).thenReturn(Optional.of(entityCreated));
        when(getRepository().save(any())).thenReturn(partialUpdated);

        String[] ignoredProperties = ValidationUtils.getNullPropertyNames(toPartialUpdate);
        T actual = assertDoesNotThrow(() -> getService().update(1L, new UpdaterExample<>(toPartialUpdate, ignoredProperties)));

        assertNotNull(actual);
        assertEquals(actual.toString(), partialUpdated.toString());
    }

    @Test
    public void givenEntity_whenPartialUpdate_theThrowEntityNotFoundException() {
        when(getService().findById(anyLong())).thenReturn(Optional.empty());

        String[] ignoredProperties = ValidationUtils.getNullPropertyNames(toPartialUpdate);
        CrudService<T> service = getService();
        UpdaterExample<T> example = new UpdaterExample<>(toPartialUpdate, ignoredProperties);

        assertThrows(EntityNotFoundException.class, () -> service.update(0L, example));
        verify(getRepository(), never()).save(any());
    }

    @Test
    public void givenEntity_whenDelete_thenDeleteEntity() {
        doNothing().when(getRepository()).deleteById(anyLong());
        when(getRepository().findById(anyLong())).thenReturn(Optional.ofNullable(entityCreated));
        assertDoesNotThrow(() -> getService().deleteById(entityCreated.getId()));
    }

    @Test
    public void givenEntity_whenDeleteById_thenThrowEntityNotFoundException() {
        when(getRepository().findById(anyLong())).thenReturn(Optional.empty());
        CrudService<T> service = getService();
        assertThrows(EntityNotFoundException.class, () -> service.deleteById(0L));
        verify(getRepository(), never()).deleteById(anyLong());
    }

}
