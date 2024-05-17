package com.astro.allcrud.integration;

import com.astro.allcrud.common.PageRequestVO;
import com.astro.allcrud.common.UpdaterExample;
import com.astro.allcrud.converter.Converter;
import com.astro.allcrud.entity.AbstractEntity;
import com.astro.allcrud.entity.AbstractEntityVO;
import com.astro.allcrud.service.CrudService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public abstract class CrudIntegrationTests<T extends AbstractEntity, VO extends AbstractEntityVO> {

    protected abstract CrudService<T> getService();

    protected abstract Converter<T, VO> getConverter();

    protected abstract void initObjects();

    @Autowired
    protected MockMvc mockMvc;

    protected String voToCreate;
    protected T entityToCreate;
    protected T createdEntity;
    protected VO voCreated;
    protected VO expected;
    protected VO newExpected;
    protected T listEntity1;
    protected T listEntity2;
    protected T emptyObject;
    protected T filterToFound1;
    protected T filterToFound2;
    protected VO voFilterToFound1;
    protected VO voFilterToFound2;
    protected T filterToNotFound;
    protected VO voFailsValidations;
    protected String voToUpdate;
    protected T toUpdate;
    protected T entityUpdated;
    protected VO voUpdated;
    protected VO voToPartialUpdate;
    protected T entityToPartialUpdate;
    protected VO voPartialUpdated;
    protected String basePath;

    protected ObjectMapper mapper;

    @BeforeEach
    public void init() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        initObjects();
        mapper = JsonMapper.builder().findAndAddModules().build();
    }

    @Test
    public void givenVO_whenCreate_thenReturnStatus201() throws JsonProcessingException {
        when(getService().create(any())).thenReturn(createdEntity);
        when(getConverter().convertToEntity(any())).thenReturn(entityToCreate);
        when(getConverter().convertToVO(any())).thenReturn(voCreated);

        given().contentType(ContentType.JSON).body(voToCreate)
                .when().post(basePath)
                .then().log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.CREATED.value())
                .body(notNullValue())
                .body(equalTo(mapper.writeValueAsString(expected)));
    }

    @Test
    public void givenVO_whenCreate_thenReturnStatus400() {
        given().contentType(ContentType.JSON).body(voFailsValidations)
                .when().post(basePath)
                .then().log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.BAD_REQUEST.value())
                .body(notNullValue())
                .body(containsString("must not be"));
    }

    @Test
    public void givenVO_whenFindById_thenReturnStatus200() throws JsonProcessingException {
        when(getService().findById(anyLong())).thenReturn(Optional.ofNullable(createdEntity));
        when(getConverter().convertToVO(any())).thenReturn(voCreated);

        given().when().get(basePath + "/" + createdEntity.getId())
                .then().log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.OK.value())
                .body(notNullValue(), not(emptyString()))
                .body(equalTo(mapper.writeValueAsString(expected)));
    }

    @Test
    public void givenVO_whenFindById_thenReturnStatus404() {
        when(getService().findById(anyLong())).thenReturn(Optional.empty());
        when(getConverter().convertToVO(any())).thenReturn(voCreated);

        given().when().get(basePath + "/0")
                .then().log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.NOT_FOUND.value())
                .body(emptyString());
    }

    @Test
    public void givenVO_whenFindAllPagedWithNoFilters_thenReturnStatus206() throws JsonProcessingException {
        Pageable page1 = PageRequest.of(0,1, Sort.Direction.ASC, "id");
        Page<T> returnedPage1 = new PageImpl<>(List.of(listEntity1), page1, 2);
        Pageable page2 = PageRequest.of(1,1, Sort.Direction.ASC, "id");
        Page<T> returnedPage2 = new PageImpl<>(List.of(listEntity2), page2, 2);
        Page<VO> expectedPage1 = new PageImpl<>(List.of(expected));
        Page<VO> expectedPage2 = new PageImpl<>(List.of(newExpected));

        when(getService().findAll(any(), eq(page1))).thenReturn(returnedPage1);
        when(getService().findAll(any(), eq(page2))).thenReturn(returnedPage2);
        when(getConverter().convertToEntity(any())).thenReturn(emptyObject);
        when(getConverter().convertToVO(listEntity1)).thenReturn(expected);
        when(getConverter().convertToVO(listEntity2)).thenReturn(newExpected);

        given().queryParam("size", 1)
                .when().get(basePath).then().log().ifValidationFails().assertThat().statusCode(HttpStatus.PARTIAL_CONTENT.value())
                .body(equalTo(mapper.writeValueAsString(expectedPage1.getContent())))
                .header(PageRequestVO.CURRENT_PAGE_HEADER, String.valueOf(0))
                .header(PageRequestVO.CURRENT_ELEMENTS_HEADER, String.valueOf(1))
                .header(PageRequestVO.TOTAL_ELEMENTS_HEADER, String.valueOf(2))
                .header(PageRequestVO.TOTAL_PAGES_HEADER, String.valueOf(2));

        given().queryParam("page", 1)
                .queryParam("size", 1)
                .when().get(basePath).then().log().ifValidationFails().assertThat().statusCode(HttpStatus.PARTIAL_CONTENT.value())
                .body(equalTo(mapper.writeValueAsString(expectedPage2.getContent())))
                .header(PageRequestVO.CURRENT_PAGE_HEADER, String.valueOf(1))
                .header(PageRequestVO.CURRENT_ELEMENTS_HEADER, String.valueOf(1))
                .header(PageRequestVO.TOTAL_ELEMENTS_HEADER, String.valueOf(2))
                .header(PageRequestVO.TOTAL_PAGES_HEADER, String.valueOf(2));
    }

    @Test
    public void givenVO_whenFindAllPagedWithAnyFilters_thenReturnStatus206() throws JsonProcessingException {
        Pageable page1 = PageRequest.of(0,1, Sort.Direction.ASC, "id");
        Page<T> returnedPage1 = new PageImpl<>(List.of(listEntity1), page1, 2);
        Page<VO> expectedPage1 = new PageImpl<>(List.of(expected), page1, 2);

        when(getService().findAll(any(), any())).thenReturn(returnedPage1);
        when(getConverter().convertToEntity(voFilterToFound1)).thenReturn(filterToFound1);
        when(getConverter().convertToVO(listEntity1)).thenReturn(expected);

        given().queryParam("size", 1)
                .queryParam("id", "1")
                .when().get(basePath).then().log().ifValidationFails().assertThat().statusCode(HttpStatus.PARTIAL_CONTENT.value())
                .body(equalTo(mapper.writeValueAsString(expectedPage1.getContent())))
                .header(PageRequestVO.CURRENT_PAGE_HEADER, String.valueOf(0))
                .header(PageRequestVO.CURRENT_ELEMENTS_HEADER, String.valueOf(1))
                .header(PageRequestVO.TOTAL_ELEMENTS_HEADER, String.valueOf(2))
                .header(PageRequestVO.TOTAL_PAGES_HEADER, String.valueOf(2));

        Pageable page2 = PageRequest.of(1,1, Sort.Direction.ASC, "id");
        Page<T> returnedPage2 = new PageImpl<>(List.of(listEntity2), page2, 2);
        Page<VO> expectedPage2 = new PageImpl<>(List.of(newExpected), page2, 2);

        when(getService().findAll(any(), any())).thenReturn(returnedPage2);
        when(getConverter().convertToEntity(voFilterToFound2)).thenReturn(filterToFound2);
        when(getConverter().convertToVO(listEntity2)).thenReturn(newExpected);

        given().queryParam("page", 1)
                .queryParam("size", 1)
                .queryParam("id", "2")
                .when().get(basePath).then().log().ifValidationFails().assertThat().statusCode(HttpStatus.PARTIAL_CONTENT.value())
                .body(equalTo(mapper.writeValueAsString(expectedPage2.getContent())))
                .header(PageRequestVO.CURRENT_PAGE_HEADER, String.valueOf(1))
                .header(PageRequestVO.CURRENT_ELEMENTS_HEADER, String.valueOf(1))
                .header(PageRequestVO.TOTAL_ELEMENTS_HEADER, String.valueOf(2))
                .header(PageRequestVO.TOTAL_PAGES_HEADER, String.valueOf(2));
    }

    @Test
    public void givenVO_whenFindAllPagedWithNotFoundFilters_thenReturnStatus204() {
        when(getService().findAll(any(), any())).thenReturn(Page.empty());
        when(getConverter().convertToEntity(any())).thenReturn(filterToNotFound);

        given().queryParam("id", "0")
                .when().get(basePath).then().log().ifValidationFails().assertThat().statusCode(HttpStatus.NO_CONTENT.value())
                .body(equalTo("[]"))
                .header(PageRequestVO.CURRENT_PAGE_HEADER, String.valueOf(0))
                .header(PageRequestVO.CURRENT_ELEMENTS_HEADER, String.valueOf(0))
                .header(PageRequestVO.TOTAL_ELEMENTS_HEADER, String.valueOf(0))
                .header(PageRequestVO.TOTAL_PAGES_HEADER, String.valueOf(1));
    }

    @Test
    public void givenIdAndVO_whenUpdate_thenReturnStatus200() throws JsonProcessingException {
        when(getService().update(any(), eq(toUpdate))).thenReturn(entityUpdated);
        when(getConverter().convertToEntity(any())).thenReturn(toUpdate);
        when(getConverter().convertToVO(any())).thenReturn(voUpdated);

        given().contentType(ContentType.JSON).body(voToUpdate)
                .when().put(basePath + "/1")
                .then().log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.OK.value())
                .body(equalTo(mapper.writeValueAsString(voUpdated)));
    }

    @Test
    public void givenIdAndVO_whenUpdate_thenReturnStatus400() {
        given().contentType(ContentType.JSON).body(voFailsValidations)
                .when().put(basePath + "/1")
                .then().log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.BAD_REQUEST.value())
                .body(not(emptyString()))
                .body(containsString("must not be"));

        verify(getConverter(), never()).convertToEntity(any());
        verify(getService(), never()).update(anyLong(), eq(toUpdate));
        verify(getConverter(), never()).convertToVO(any());
    }

    @Test
    public void givenIdAndVO_whenUpdate_thenReturnStatus404() {
        when(getService().update(any(), eq(toUpdate))).thenThrow(EntityNotFoundException.class);
        when(getConverter().convertToEntity(any())).thenReturn(toUpdate);
        when(getConverter().convertToVO(any())).thenReturn(voUpdated);

        given().contentType(ContentType.JSON).body(voToUpdate)
                .when().put(basePath + "/0")
                .then().log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.NOT_FOUND.value())
                .body(emptyString());

        verify(getConverter(), never()).convertToVO(any());
    }

    @Test
    public void givenIdAndVO_whenPartialUpdate_thenReturnStatus200() throws JsonProcessingException {
        when(getService().update(any(), any(UpdaterExample.class))).thenReturn(entityUpdated);
        when(getConverter().convertToEntity(any())).thenReturn(entityToPartialUpdate);
        when(getConverter().convertToVO(any())).thenReturn(voPartialUpdated);

        given().contentType(ContentType.JSON).body(voToPartialUpdate)
                .when().patch(basePath + "/1")
                .then().log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.OK.value())
                .body(equalTo(mapper.writeValueAsString(voPartialUpdated)));
    }

    @Test
    public void givenIdAndVO_whenPartialUpdate_thenReturnStatus404() {
        when(getService().update(any(), any(UpdaterExample.class))).thenThrow(EntityNotFoundException.class);
        when(getConverter().convertToEntity(any())).thenReturn(toUpdate);

        given().contentType(ContentType.JSON).body(voToUpdate)
                .when().patch(basePath + "/0")
                .then().log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.NOT_FOUND.value())
                .body(emptyString());

        verify(getConverter(), never()).convertToVO(any());
    }

    @Test
    public void givenId_whenDelete_thenReturnStatus204() {
        doNothing().when(getService()).deleteById(anyLong());

        given().when().delete(basePath + "/1")
                .then().log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.NO_CONTENT.value())
                .body(emptyString());
    }

    @Test
    public void givenId_whenDelete_thenReturnStatus404() {
        doThrow(EntityNotFoundException.class).when(getService()).deleteById(anyLong());

        given().when().delete(basePath + "/1")
                .then().log().ifValidationFails()
                .assertThat().statusCode(HttpStatus.NOT_FOUND.value())
                .body(emptyString());
    }

}
