package com.techmath.allcrud.controller;

import com.techmath.allcrud.common.PageRequestVO;
import com.techmath.allcrud.common.UpdaterExample;
import com.techmath.allcrud.converter.Converter;
import com.techmath.allcrud.entity.AbstractEntity;
import com.techmath.allcrud.entity.AbstractEntityVO;
import com.techmath.allcrud.service.CrudService;
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
public abstract class CrudControllerTests<T extends AbstractEntity, VO extends AbstractEntityVO> {

    private final Class<T> entityClass;
    private final Class<VO> voClass;

    protected abstract CrudService<T> getService();

    protected abstract Converter<T, VO> getConverter();

    protected abstract CrudController<T, VO> getController();

    protected CrudControllerTests(Class<T> entityClass, Class<VO> voClass) {
        this.entityClass = entityClass;
        this.voClass = voClass;
    }

    @Test
    public void givenEntity_whenCreate_thenReturnCreatedEntity() {
        VO voCreated = Instancio.create(voClass);
        VO voToCreate = Instancio.createBlank(voClass);
        BeanUtils.copyProperties(voCreated, voToCreate);
        voToCreate.setId(null);

        T createdEntity = getConverter().convertToEntity(voCreated);
        when(getService().create(any())).thenReturn(createdEntity);

        VO actual = getController().create(voToCreate);

        assertNotNull(actual);
        assertEquals(voCreated.toString(), actual.toString());
    }

    @Test
    public void givenEntity_whenCreate_thenThrowEntityExistsException() {
        VO voToCreate = Instancio.create(voClass);
        when(getService().create(any())).thenThrow(EntityExistsException.class);

        CrudController<T, VO> controller = getController();
        assertThrows(EntityExistsException.class, () -> controller.create(voToCreate));
    }

    @Test
    public void givenId_whenFindById_thenReturnEntity() {
        T createdEntity = Instancio.create(entityClass);
        VO voCreated = getConverter().convertToVO(createdEntity);
        when(getService().findById(anyLong())).thenReturn(Optional.ofNullable(createdEntity));

        VO actual = getController().findById(1L);

        assertNotNull(actual);
        assertEquals(voCreated.toString(), actual.toString());
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
        VO emptyFilter = Instancio.createBlank(voClass);
        T entity1 = Instancio.create(entityClass);
        VO voEntity1 = getConverter().convertToVO(entity1);
        T entity2 = Instancio.create(entityClass);
        VO voEntity2 = getConverter().convertToVO(entity2);

        Page<T> expectedPage1 = new PageImpl<>(List.of(entity1));
        Page<VO> voExpectedPage1 = new PageImpl<>(List.of(voEntity1));
        Pageable page1 = PageRequest.of(0, 1, Sort.Direction.ASC, "id");
        PageRequestVO page1VO = PageRequestVO.builder().page(page1.getPageNumber()).size(page1.getPageSize())
                .direction(Sort.Direction.ASC).orderBy("id").build();

        when(getService().findAll(any(), eq(page1))).thenReturn(expectedPage1);

        List<VO> actualPage1 = getController().findAll(emptyFilter, page1VO).getBody();

        Page<T> expectedPage2 = new PageImpl<>(List.of(entity2));
        Page<VO> voExpectedPage2 = new PageImpl<>(List.of(voEntity2));
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
        T entity = Instancio.create(entityClass);
        VO vo = getConverter().convertToVO(entity);
        VO filters = Instancio.create(voClass);

        Page<T> expectedPage1 = new PageImpl<>(List.of(entity));
        Page<VO> voExpectedPage1 = new PageImpl<>(List.of(vo));
        Pageable page1 = PageRequest.of(0, 1, Sort.Direction.ASC, "id");
        PageRequestVO page1VO = PageRequestVO.builder().page(page1.getPageNumber()).size(page1.getPageSize())
                .direction(Sort.Direction.ASC).orderBy("id").build();

        when(getService().findAll(any(), eq(page1))).thenReturn(expectedPage1);

        List<VO> actualPage1 = getController().findAll(filters, page1VO).getBody();

        assertNotNull(actualPage1);
        assertFalse(actualPage1.isEmpty());
        assertEquals(1, actualPage1.size());
        assertArrayEquals(voExpectedPage1.getContent().toArray(), actualPage1.toArray());
    }

    @Test
    public void givenEntity_whenFindAllWithNotFoundFilters_thenReturnPagedEmpty() {
        VO notFoundFilters = Instancio.create(voClass);

        Pageable page1 = PageRequest.of(0, 1, Sort.Direction.ASC, "id");
        PageRequestVO page1VO = PageRequestVO.builder().page(page1.getPageNumber()).size(page1.getPageSize())
                .direction(Sort.Direction.ASC).orderBy("id").build();

        when(getService().findAll(any(), eq(page1))).thenReturn(Page.empty());

        List<VO> actualPage1 = getController().findAll(notFoundFilters, page1VO).getBody();

        assertNotNull(actualPage1);
        assertTrue(actualPage1.isEmpty());
        verify(getConverter(), never()).convertToVO(any());
    }

    @Test
    public void givenEntity_whenUpdate_thenReturnUpdatedEntity() {
        T entityUpdated = Instancio.create(entityClass);
        VO voUpdated = getConverter().convertToVO(entityUpdated);
        when(getService().update(anyLong(), any(entityClass))).thenReturn(entityUpdated);

        VO actual = assertDoesNotThrow(() -> getController().update(1L, voUpdated));

        assertNotNull(actual);
        assertEquals(voUpdated.toString(), actual.toString());
    }

    @Test
    public void givenEntity_whenUpdate_thenThrowEntityNotFoundException() {
        VO voToUpdate = Instancio.create(voClass);
        when(getService().update(anyLong(), any(entityClass))).thenThrow(EntityNotFoundException.class);

        CrudController<T, VO> controller = getController();
        assertThrows(EntityNotFoundException.class, () -> controller.update(3L, voToUpdate));
        verify(getConverter(), never()).convertToVO(any());
    }

    @Test
    public void givenEntity_whenPartialUpdate_thenReturnUpdatedEntity() {
        T entityPartialUpdated = Instancio.create(entityClass);
        VO voPartialUpdated = getConverter().convertToVO(entityPartialUpdated);
        when(getService().update(anyLong(), any(UpdaterExample.class))).thenReturn(entityPartialUpdated);

        VO actual = assertDoesNotThrow(() -> getController().partialUpdate(1L, voPartialUpdated));

        assertNotNull(actual);
        assertEquals(voPartialUpdated.toString(), actual.toString());
    }

    @Test
    public void givenEntity_whenPartialUpdate_thenThrowEntityNotFoundException() {
        VO voToPartialUpdate = Instancio.create(voClass);
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
