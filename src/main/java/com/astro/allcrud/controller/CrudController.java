package com.astro.allcrud.controller;

import com.astro.allcrud.common.PageRequestVO;
import com.astro.allcrud.common.UpdaterExample;
import com.astro.allcrud.converter.Converter;
import com.astro.allcrud.entity.AbstractEntity;
import com.astro.allcrud.entity.AbstractEntityVO;
import com.astro.allcrud.enums.CrudErrorMessage;
import com.astro.allcrud.service.CrudService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Supplier;

public abstract class CrudController<T extends AbstractEntity, VO extends AbstractEntityVO> {

    protected abstract CrudService<T> getService();

    protected abstract Converter<T, VO> getConverter();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VO create(@Valid @RequestBody VO vo) {
        T entity = getConverter().convertToEntity(vo);
        T created = getService().create(entity);
        return getConverter().convertToVO(created);
    }

    @GetMapping(value = "/{id}")
    public VO findById(@PathVariable Long id) {
        T entity = getService().findById(id).orElseThrow(entityNotFoundExceptionSupplier(id));
        return getConverter().convertToVO(entity);
    }

    @GetMapping
    public ResponseEntity<List<VO>> findAll(VO filters, PageRequestVO pageable) {
        T entityFilters = getConverter().convertToEntity(filters);
        Page<T> result = getService().findAll(entityFilters, getPageableOf(pageable));
        HttpHeaders headers = mountPageableHttpHeaders(pageable, result);
        List<VO> content = result.map(getConverter()::convertToVO).getContent();
        HttpStatus status = result.getTotalElements() == 0 ? HttpStatus.NO_CONTENT : HttpStatus.PARTIAL_CONTENT;
        return new ResponseEntity<>(content, headers, status);
    }

    @PutMapping(value = "/{id}")
    public VO update(@PathVariable Long id, @Valid @RequestBody VO vo) {
        T toUpdate = getConverter().convertToEntity(vo);
        T updated = getService().update(id, toUpdate);
        return getConverter().convertToVO(updated);
    }

    @PatchMapping(value = "/{id}")
    public VO partialUpdate(@PathVariable Long id, @RequestBody VO vo) {
        T toUpdate = getConverter().convertToEntity(vo);
        T updated = getService().update(id, new UpdaterExample<>(toUpdate));
        return getConverter().convertToVO(updated);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        getService().deleteById(id);
    }

    //*****************************************************************************************************************
    //******************************************* PRIVATE/PROTECTED METHODS *******************************************
    //*****************************************************************************************************************

    protected Pageable getPageableOf(PageRequestVO vo) {
        return PageRequest.of(vo.getPage(), vo.getSize(), vo.getDirection(), vo.getOrderBy());
    }

    protected HttpHeaders mountPageableHttpHeaders(PageRequestVO pageable, Page<T> result) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(PageRequestVO.CURRENT_PAGE_HEADER, String.valueOf(pageable.getPage()));
        headers.add(PageRequestVO.CURRENT_ELEMENTS_HEADER, String.valueOf(result.getNumberOfElements()));
        headers.add(PageRequestVO.TOTAL_ELEMENTS_HEADER, String.valueOf(result.getTotalElements()));
        headers.add(PageRequestVO.TOTAL_PAGES_HEADER, String.valueOf(result.getTotalPages()));
        return headers;
    }

    protected Supplier<EntityNotFoundException> entityNotFoundExceptionSupplier(Long id) {
        var message = String.format(CrudErrorMessage.ENTITY_NOT_FOUND_MESSAGE.getMessage(), id);
        return () -> new EntityNotFoundException(message);
    }

}
