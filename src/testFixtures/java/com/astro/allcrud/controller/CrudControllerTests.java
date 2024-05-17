package com.astro.allcrud.controller;

import com.astro.allcrud.common.PageRequestVO;
import com.astro.allcrud.common.UpdaterExample;
import com.astro.allcrud.converter.Converter;
import com.astro.allcrud.entity.AbstractEntity;
import com.astro.allcrud.entity.AbstractEntityVO;
import com.astro.allcrud.service.CrudService;
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
public abstract class CrudControllerTests<T extends AbstractEntity, VO extends AbstractEntityVO> {

    protected abstract CrudService<T> getService();

    protected abstract Converter<T, VO> getConverter();

    protected abstract CrudController<T, VO> getController();

    protected VO voToCreate;
    protected T createdEntity;
    protected VO voConverted;
    protected VO expected;
    protected T secondEntity;
    protected VO voSecondEntity;
    protected VO emptyFilter;
    protected VO voFilter;
    protected VO voFilterNotFound;
    protected T entityToUpdate;
    protected VO voToUpdate;
    protected T entityUpdated;
    protected VO expectedVoUpdated;
    protected VO voToPartialUpdate;
    protected T entityPartialUpdated;
    protected VO voPartialUpdated;

    @Test
    public void givenEntity_whenCreate_thenReturnCreatedEntity() {
        when(getService().create(any())).thenReturn(createdEntity);

        VO actual = getController().create(voToCreate);

        assertNotNull(actual);
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void givenEntity_whenCreate_thenThrowEntityExistsException() {
        when(getService().create(any())).thenThrow(EntityExistsException.class);

        CrudController<T, VO> controller = getController();
        assertThrows(EntityExistsException.class, () -> controller.create(voToCreate));
    }

    @Test
    public void givenId_whenFindById_thenReturnEntity() {
        when(getService().findById(anyLong())).thenReturn(Optional.ofNullable(createdEntity));

        VO actual = getController().findById(1L);

        assertNotNull(actual);
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void givenId_whenFindById_thenThrowEntityNotFoundException() {
        when(getService().findById(anyLong())).thenReturn(Optional.empty());
        CrudController<T, VO> controller = getController();
        assertThrows(EntityNotFoundException.class, () -> controller.findById(0L));
        verify(getConverter(), never()).convertToVO(any());
    }

    @Test
    public void givenEntity_whenFindAllWithNoFilters_thenReturnPagedAllEntities() {
        Page<T> expectedPage1 = new PageImpl<>(List.of(createdEntity));
        Page<VO> voExpectedPage1 = new PageImpl<>(List.of(voConverted));
        Pageable page1 = PageRequest.of(0, 1, Sort.Direction.ASC, "id");
        PageRequestVO page1VO = PageRequestVO.builder().page(page1.getPageNumber()).size(page1.getPageSize())
                .direction(Sort.Direction.ASC).orderBy("id").build();

        when(getService().findAll(any(), eq(page1))).thenReturn(expectedPage1);

        List<VO> actualPage1 = getController().findAll(emptyFilter, page1VO).getBody();

        Page<T> expectedPage2 = new PageImpl<>(List.of(secondEntity));
        Page<VO> voExpectedPage2 = new PageImpl<>(List.of(voSecondEntity));
        Pageable page2 = PageRequest.of(1, 1, Sort.Direction.ASC, "id");
        PageRequestVO page2VO = PageRequestVO.builder().page(page2.getPageNumber()).size(page2.getPageSize())
                .direction(Sort.Direction.ASC).orderBy("id").build();

        when(getService().findAll(any(), eq(page2))).thenReturn(expectedPage2);

        List<VO> actualPage2 = getController().findAll(emptyFilter, page2VO).getBody();

        assertNotNull(actualPage1);
        assertNotNull(actualPage2);
        assertFalse(actualPage1.isEmpty());
        assertFalse(actualPage2.isEmpty());
        assertEquals(1, actualPage1.size());
        assertEquals(1, actualPage2.size());
        assertArrayEquals(voExpectedPage1.getContent().toArray(), actualPage1.toArray());
        assertArrayEquals(voExpectedPage2.getContent().toArray(), actualPage2.toArray());
    }

    @Test
    public void givenEntity_whenFindAllWithAnyFilters_thenReturnPagedFilteredEntities() {
        Page<T> expectedPage1 = new PageImpl<>(List.of(createdEntity));
        Page<VO> voExpectedPage1 = new PageImpl<>(List.of(voConverted));
        Pageable page1 = PageRequest.of(0, 1, Sort.Direction.ASC, "id");
        PageRequestVO page1VO = PageRequestVO.builder().page(page1.getPageNumber()).size(page1.getPageSize())
                .direction(Sort.Direction.ASC).orderBy("id").build();

        when(getService().findAll(any(), eq(page1))).thenReturn(expectedPage1);

        List<VO> actualPage1 = getController().findAll(voFilter, page1VO).getBody();

        assertNotNull(actualPage1);
        assertFalse(actualPage1.isEmpty());
        assertEquals(1, actualPage1.size());
        assertArrayEquals(voExpectedPage1.getContent().toArray(), actualPage1.toArray());
    }

    @Test
    public void givenEntity_whenFindAllWithNotFoundFilters_thenReturnPagedEmpty() {
        Pageable page1 = PageRequest.of(0, 1, Sort.Direction.ASC, "id");
        PageRequestVO page1VO = PageRequestVO.builder().page(page1.getPageNumber()).size(page1.getPageSize())
                .direction(Sort.Direction.ASC).orderBy("id").build();

        when(getService().findAll(any(), eq(page1))).thenReturn(Page.empty());

        List<VO> actualPage1 = getController().findAll(voFilterNotFound, page1VO).getBody();

        assertNotNull(actualPage1);
        assertTrue(actualPage1.isEmpty());
        verify(getConverter(), never()).convertToVO(any());
    }

    @Test
    public void givenEntity_whenUpdate_thenReturnUpdatedEntity() {
        when(getService().update(anyLong(), eq(entityToUpdate))).thenReturn(entityUpdated);

        VO actual = assertDoesNotThrow(() -> getController().update(1L, voToUpdate));
        assertNotNull(actual);
        assertEquals(expectedVoUpdated.toString(), actual.toString());
    }

    @Test
    public void givenEntity_whenUpdate_thenThrowEntityNotFoundException() {
        when(getService().update(anyLong(), eq(entityToUpdate))).thenThrow(EntityNotFoundException.class);

        CrudController<T, VO> controller = getController();
        assertThrows(EntityNotFoundException.class, () -> controller.update(3L, voToUpdate));
        verify(getConverter(), never()).convertToVO(any());
    }

    @Test
    public void givenEntity_whenPartialUpdate_thenReturnUpdatedEntity() {
        when(getService().update(anyLong(), any(UpdaterExample.class))).thenReturn(entityPartialUpdated);

        VO actual = assertDoesNotThrow(() -> getController().partialUpdate(1L, voToPartialUpdate));
        assertNotNull(actual);
        assertEquals(voPartialUpdated.toString(), actual.toString());
    }

    @Test
    public void givenEntity_whenPartialUpdate_thenThrowEntityNotFoundException() {
        when(getService().update(anyLong(), any(UpdaterExample.class))).thenThrow(EntityNotFoundException.class);

        CrudController<T, VO> controller = getController();
        assertThrows(EntityNotFoundException.class, () -> controller.partialUpdate(3L, voToPartialUpdate));
        verify(getConverter(), never()).convertToVO(any());
    }

    @Test
    public void givenEntity_whenDeleteById_thenDeleteEntity() {
        doNothing().when(getService()).deleteById(anyLong());
        assertDoesNotThrow(() -> getController().deleteById(1L));
    }

    @Test
    public void givenEntity_whenDeleteById_thenThrowEntityNotFoundException() {
        doThrow(EntityNotFoundException.class).when(getService()).deleteById(anyLong());
        CrudController<T, VO> controller = getController();
        assertThrows(EntityNotFoundException.class, () -> controller.deleteById(3L));
    }

}
