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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Abstract base class for unit testing controllers that extend {@link CrudController}.
 * <p>
 * This class validates the behavior of each endpoint in isolation using mocked dependencies.
 * All HTTP methods are tested via direct controller calls.
 * <p>
 * To use, extend this class in your controller test, provide mocks via {@code @Mock},
 * and override {@link #getController()}, {@link #getService()} and {@link #getConverter()}.
 *
 * @param <T>  the type of the entity. Must extend {@link AbstractEntity}.
 * @param <VO> the type of the value object. Must extend {@link AbstractEntityVO}.
 *
 * @see com.techmath.allcrud.controller.CrudController
 * @see com.techmath.allcrud.service.CrudService
 * @see com.techmath.allcrud.converter.Converter
 *
 * @author Matheus Maia
 */
@SuppressWarnings("unchecked")
public abstract class CrudControllerTests<T extends AbstractEntity<ID>, VO extends AbstractEntityVO<ID>, ID> {

    private final Class<T> entityClass;
    private final Class<VO> voClass;
    private final Class<ID> idClass;

    protected abstract CrudService<T, ID> getService();

    protected abstract Converter<T, VO, ID> getConverter();

    protected abstract CrudController<T, VO, ID> getController();

    protected CrudControllerTests(Class<T> entityClass, Class<VO> voClass, Class<ID> idClass) {
        this.entityClass = entityClass;
        this.voClass = voClass;
        this.idClass = idClass;
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

        CrudController<T, VO, ID> controller = getController();
        assertThrows(EntityExistsException.class, () -> controller.create(voToCreate));
    }

    @Test
    public void givenId_whenFindById_thenReturnEntity() {
        T createdEntity = Instancio.create(entityClass);
        ID id = Instancio.create(idClass);
        VO voCreated = getConverter().convertToVO(createdEntity);
        when(getService().findById(any())).thenReturn(Optional.ofNullable(createdEntity));

        VO actual = getController().findById(id);

        assertNotNull(actual);
        assertEquals(voCreated.toString(), actual.toString());
    }

    @Test
    public void givenId_whenFindById_thenThrowEntityNotFoundException() {
        ID id = Instancio.create(idClass);
        when(getService().findById(any())).thenReturn(Optional.empty());
        CrudController<T, VO, ID> controller = getController();
        assertThrows(EntityNotFoundException.class, () -> controller.findById(id));
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
        ID id = Instancio.create(idClass);
        when(getService().update(any(), any(entityClass))).thenReturn(entityUpdated);

        VO actual = assertDoesNotThrow(() -> getController().update(id, voUpdated));

        assertNotNull(actual);
        assertEquals(voUpdated.toString(), actual.toString());
    }

    @Test
    public void givenEntity_whenUpdate_thenThrowEntityNotFoundException() {
        VO voToUpdate = Instancio.create(voClass);
        ID id = Instancio.create(idClass);
        when(getService().update(any(), any(entityClass))).thenThrow(EntityNotFoundException.class);

        CrudController<T, VO, ID> controller = getController();
        assertThrows(EntityNotFoundException.class, () -> controller.update(id, voToUpdate));
        verify(getConverter(), never()).convertToVO(any());
    }

    @Test
    public void givenEntity_whenPartialUpdate_thenReturnUpdatedEntity() {
        T entityPartialUpdated = Instancio.create(entityClass);
        VO voPartialUpdated = getConverter().convertToVO(entityPartialUpdated);
        ID id = Instancio.create(idClass);
        when(getService().update(any(), any(UpdaterExample.class))).thenReturn(entityPartialUpdated);

        VO actual = assertDoesNotThrow(() -> getController().partialUpdate(id, voPartialUpdated));

        assertNotNull(actual);
        assertEquals(voPartialUpdated.toString(), actual.toString());
    }

    @Test
    public void givenEntity_whenPartialUpdate_thenThrowEntityNotFoundException() {
        VO voToPartialUpdate = Instancio.create(voClass);
        ID id = Instancio.create(idClass);
        when(getService().update(any(), any(UpdaterExample.class))).thenThrow(EntityNotFoundException.class);

        CrudController<T, VO, ID> controller = getController();
        assertThrows(EntityNotFoundException.class, () -> controller.partialUpdate(id, voToPartialUpdate));
        verify(getConverter(), never()).convertToVO(any());
    }

    @Test
    public void givenEntity_whenDeleteById_thenDeleteEntity() {
        ID id = Instancio.create(idClass);
        doNothing().when(getService()).deleteById(any());
        assertDoesNotThrow(() -> getController().deleteById(id));
    }

    @Test
    public void givenEntity_whenDeleteById_thenThrowEntityNotFoundException() {
        ID id = Instancio.create(idClass);
        doThrow(EntityNotFoundException.class).when(getService()).deleteById(any());
        CrudController<T, VO, ID> controller = getController();
        assertThrows(EntityNotFoundException.class, () -> controller.deleteById(id));
    }

}
