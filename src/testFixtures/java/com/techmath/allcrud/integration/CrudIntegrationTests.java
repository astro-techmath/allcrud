package com.techmath.allcrud.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.techmath.allcrud.common.PageRequestVO;
import com.techmath.allcrud.common.UpdaterExample;
import com.techmath.allcrud.converter.Converter;
import com.techmath.allcrud.entity.AbstractEntity;
import com.techmath.allcrud.entity.AbstractEntityVO;
import com.techmath.allcrud.service.CrudService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import jakarta.persistence.EntityNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.instancio.Instancio;
import org.instancio.settings.Keys;
import org.instancio.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Abstract base class for full-stack integration tests of {@link com.techmath.allcrud.controller.CrudController}.
 * <p>
 * This test class validates the HTTP contract of all generic CRUD endpoints using
 * {@link io.restassured.module.mockmvc.RestAssuredMockMvc} and {@link org.springframework.test.web.servlet.MockMvc}.
 * <p>
 * It includes built-in assertions for:
 * <ul>
 *   <li>{@code POST} create (201, 400)</li>
 *   <li>{@code GET} by ID (200, 404)</li>
 *   <li>{@code GET} paginated list (204, 206)</li>
 *   <li>{@code PUT} full update (200, 400, 404)</li>
 *   <li>{@code PATCH} partial update (200, 404)</li>
 *   <li>{@code DELETE} by ID (204, 404)</li>
 * </ul>
 *
 * <p>
 * By default, it uses {@link org.instancio.Instancio} for object generation with Bean Validation enabled.
 * Pagination headers and custom status codes (204, 206) are also asserted.
 *
 * <p>
 * If the controller class is passed to the constructor, the {@code basePath} is automatically resolved
 * via reflection by reading the {@link org.springframework.web.bind.annotation.RequestMapping} annotation.
 * If needed, this value can be overridden manually via the protected {@code basePath} field.
 * <h3>Usage</h3>
 * Extend this class and:
 * <ul>
 *   <li>Annotate your test class with {@code @WebMvcTest} and {@code @AutoConfigureMockMvc}</li>
 *   <li>Provide entity and VO class via constructor</li>
 *   <li>Override {@link #getService()} and {@link #getConverter()}</li>
 *   <li>Optionally provide the controller class to resolve {@code basePath} via annotation</li>
 * </ul>
 *
 * @param <T>  the type of the entity. Must be a subclass of {@link AbstractEntity}.
 * @param <VO> the type of the value object. Must be a subclass of {@link AbstractEntityVO}.
 *
 * @see com.techmath.allcrud.controller.CrudController
 * @see com.techmath.allcrud.service.CrudService
 * @see com.techmath.allcrud.converter.Converter
 * @see com.techmath.allcrud.common.PageRequestVO
 * @see org.instancio.Instancio
 *
 * @author Matheus Maia
 */
@SuppressWarnings("unchecked")
public abstract class CrudIntegrationTests<T extends AbstractEntity<ID>, VO extends AbstractEntityVO<ID>, ID> {

    private final Class<T> entityClass;
    private final Class<VO> voClass;
    private final Class<ID> idClass;
    protected String basePath;

    protected abstract CrudService<T, ID> getService();

    protected abstract Converter<T, VO, ID> getConverter();

    protected CrudIntegrationTests(Class<T> entityClass, Class<VO> voClass, Class<ID> idClass, Class<?> controllerClass) {
        this.entityClass = entityClass;
        this.voClass = voClass;
        this.idClass = idClass;
        this.basePath = resolveBasePath(controllerClass);
    }

    protected CrudIntegrationTests(Class<T> entityClass, Class<VO> voClass, Class<ID> idClass) {
        this.entityClass = entityClass;
        this.voClass = voClass;
        this.idClass = idClass;
        this.basePath = StringUtils.EMPTY;
    }

    private String resolveBasePath(Class<?> controllerClass) {
        RequestMapping mapping = controllerClass.getAnnotation(RequestMapping.class);
        if (Objects.nonNull(mapping) && mapping.value().length > 0) {
            return mapping.value()[0];
        }
        return StringUtils.EMPTY;
    }

    @Autowired
    protected MockMvc mockMvc;

    private T entityToCreate;
    private VO voToCreate;
    private T createdEntity;
    private VO voCreated;
    private T entityUpdated;
    private VO voUpdated;
    private T entityToUpdate;
    private VO voToUpdate;
    private VO voPartialUpdated;

    protected Settings settings;
    protected ObjectMapper mapper;

    @BeforeEach
    public void init() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        mapper = JsonMapper.builder().findAndAddModules().build();
        settings = Settings.create().set(Keys.BEAN_VALIDATION_ENABLED, true);
        createdEntity = Instancio.of(entityClass).withSettings(settings).create();
        voCreated = Instancio.of(voClass).withSettings(settings).create();
        entityToCreate = Instancio.of(entityClass).withSettings(settings).create();
        voToCreate = Instancio.of(voClass).withSettings(settings).create();
        entityUpdated = Instancio.of(entityClass).withSettings(settings).create();
        voUpdated = Instancio.of(voClass).withSettings(settings).create();
        entityToUpdate = Instancio.of(entityClass).withSettings(settings).create();
        voToUpdate = Instancio.of(voClass).withSettings(settings).create();
        voPartialUpdated = Instancio.of(voClass).withSettings(settings).create();
    }

    @Test
    public void givenVO_whenCreate_thenReturnStatus201() throws JsonProcessingException {
        when(getService().create(any())).thenReturn(createdEntity);
        when(getConverter().convertToEntity(any())).thenReturn(entityToCreate);
        when(getConverter().convertToVO(createdEntity)).thenReturn(voCreated);

        given().contentType(ContentType.JSON).body(voToCreate)
                .when().post(basePath)
                .then().log().ifValidationFails()
                .assertThat().status(HttpStatus.CREATED)
                .body(notNullValue())
                .body(equalTo(mapper.writeValueAsString(voCreated)));
    }

    @Test
    public void givenVO_whenCreate_thenReturnStatus400() {
        VO voFailsValidations = Instancio.createBlank(voClass);
        given().contentType(ContentType.JSON).body(voFailsValidations)
                .when().post(basePath)
                .then().log().ifValidationFails()
                .assertThat().status(HttpStatus.BAD_REQUEST)
                .body(notNullValue());
    }

    @Test
    public void givenVO_whenFindById_thenReturnStatus200() throws JsonProcessingException {
        ID id = Instancio.create(idClass);
        when(getService().findById(any())).thenReturn(Optional.ofNullable(createdEntity));
        when(getConverter().convertToVO(any())).thenReturn(voCreated);

        given().when().get(basePath + "/" + id)
                .then().log().ifValidationFails()
                .assertThat().status(HttpStatus.OK)
                .body(notNullValue(), not(emptyString()))
                .body(equalTo(mapper.writeValueAsString(voCreated)));
    }

    @Test
    public void givenVO_whenFindById_thenReturnStatus404() {
        ID id = Instancio.create(idClass);
        when(getService().findById(any())).thenReturn(Optional.empty());
        when(getConverter().convertToVO(any())).thenReturn(voCreated);

        given().when().get(basePath + "/" + id)
                .then().log().ifValidationFails()
                .assertThat().status(HttpStatus.NOT_FOUND)
                .body(not(emptyString()));
    }

    @Test
    public void givenVO_whenFindAllPagedWithNoFilters_thenReturnStatus206() throws JsonProcessingException {
        T listEntity1 = Instancio.create(entityClass);
        T listEntity2 = Instancio.create(entityClass);
        VO voEntity1 = Instancio.create(voClass);
        VO voEntity2 = Instancio.create(voClass);
        T emptyObject = Instancio.createBlank(entityClass);

        Pageable page1 = PageRequest.of(0,1, Sort.Direction.ASC, "id");
        Page<T> returnedPage1 = new PageImpl<>(List.of(listEntity1), page1, 2);
        Pageable page2 = PageRequest.of(1,1, Sort.Direction.ASC, "id");
        Page<T> returnedPage2 = new PageImpl<>(List.of(listEntity2), page2, 2);
        Page<VO> expectedPage1 = new PageImpl<>(List.of(voEntity1));
        Page<VO> expectedPage2 = new PageImpl<>(List.of(voEntity2));

        when(getService().findAll(any(), eq(page1))).thenReturn(returnedPage1);
        when(getService().findAll(any(), eq(page2))).thenReturn(returnedPage2);
        when(getConverter().convertToEntity(any())).thenReturn(emptyObject);
        when(getConverter().convertToVO(listEntity1)).thenReturn(voEntity1);
        when(getConverter().convertToVO(listEntity2)).thenReturn(voEntity2);

        given().queryParam("size", 1)
                .when().get(basePath).then().log().ifValidationFails().assertThat().status(HttpStatus.PARTIAL_CONTENT)
                .body(equalTo(mapper.writeValueAsString(expectedPage1.getContent())))
                .header(PageRequestVO.CURRENT_PAGE_HEADER, String.valueOf(0))
                .header(PageRequestVO.CURRENT_ELEMENTS_HEADER, String.valueOf(1))
                .header(PageRequestVO.TOTAL_ELEMENTS_HEADER, String.valueOf(2))
                .header(PageRequestVO.TOTAL_PAGES_HEADER, String.valueOf(2));

        given().queryParam("page", 1)
                .queryParam("size", 1)
                .when().get(basePath).then().log().ifValidationFails().assertThat().status(HttpStatus.PARTIAL_CONTENT)
                .body(equalTo(mapper.writeValueAsString(expectedPage2.getContent())))
                .header(PageRequestVO.CURRENT_PAGE_HEADER, String.valueOf(1))
                .header(PageRequestVO.CURRENT_ELEMENTS_HEADER, String.valueOf(1))
                .header(PageRequestVO.TOTAL_ELEMENTS_HEADER, String.valueOf(2))
                .header(PageRequestVO.TOTAL_PAGES_HEADER, String.valueOf(2));
    }

    @Test
    public void givenVO_whenFindAllPagedWithAnyFilters_thenReturnStatus206() throws JsonProcessingException {
        T listEntity1 = Instancio.create(entityClass);
        VO voEntity1 = Instancio.create(voClass);
        T filterToFound1 = Instancio.create(entityClass);
        VO voFilterToFound1 = Instancio.create(voClass);

        Pageable page1 = PageRequest.of(0,1, Sort.Direction.ASC, "id");
        Page<T> returnedPage1 = new PageImpl<>(List.of(listEntity1), page1, 2);
        Page<VO> expectedPage1 = new PageImpl<>(List.of(voEntity1), page1, 2);

        when(getService().findAll(any(), any())).thenReturn(returnedPage1);
        when(getConverter().convertToEntity(voFilterToFound1)).thenReturn(filterToFound1);
        when(getConverter().convertToVO(listEntity1)).thenReturn(voEntity1);

        given().queryParam("size", 1)
                .when().get(basePath).then().log().ifValidationFails().assertThat().status(HttpStatus.PARTIAL_CONTENT)
                .body(equalTo(mapper.writeValueAsString(expectedPage1.getContent())))
                .header(PageRequestVO.CURRENT_PAGE_HEADER, String.valueOf(0))
                .header(PageRequestVO.CURRENT_ELEMENTS_HEADER, String.valueOf(1))
                .header(PageRequestVO.TOTAL_ELEMENTS_HEADER, String.valueOf(2))
                .header(PageRequestVO.TOTAL_PAGES_HEADER, String.valueOf(2));

        T listEntity2 = Instancio.create(entityClass);
        VO voEntity2 = Instancio.create(voClass);
        T filterToFound2 = Instancio.create(entityClass);
        VO voFilterToFound2 = Instancio.create(voClass);

        Pageable page2 = PageRequest.of(1,1, Sort.Direction.ASC, "id");
        Page<T> returnedPage2 = new PageImpl<>(List.of(listEntity2), page2, 2);
        Page<VO> expectedPage2 = new PageImpl<>(List.of(voEntity2), page2, 2);

        when(getService().findAll(any(), any())).thenReturn(returnedPage2);
        when(getConverter().convertToEntity(voFilterToFound2)).thenReturn(filterToFound2);
        when(getConverter().convertToVO(listEntity2)).thenReturn(voEntity2);

        given().queryParam("page", 1)
                .queryParam("size", 1)
                .when().get(basePath).then().log().ifValidationFails().assertThat().status(HttpStatus.PARTIAL_CONTENT)
                .body(equalTo(mapper.writeValueAsString(expectedPage2.getContent())))
                .header(PageRequestVO.CURRENT_PAGE_HEADER, String.valueOf(1))
                .header(PageRequestVO.CURRENT_ELEMENTS_HEADER, String.valueOf(1))
                .header(PageRequestVO.TOTAL_ELEMENTS_HEADER, String.valueOf(2))
                .header(PageRequestVO.TOTAL_PAGES_HEADER, String.valueOf(2));
    }

    @Test
    public void givenVO_whenFindAllPagedWithNotFoundFilters_thenReturnStatus204() {
        T filterToNotFound = Instancio.create(entityClass);

        when(getService().findAll(any(), any())).thenReturn(Page.empty());
        when(getConverter().convertToEntity(any())).thenReturn(filterToNotFound);

        given().when().get(basePath).then().log().ifValidationFails().assertThat().status(HttpStatus.NO_CONTENT)
                .body(equalTo("[]"))
                .header(PageRequestVO.CURRENT_PAGE_HEADER, String.valueOf(0))
                .header(PageRequestVO.CURRENT_ELEMENTS_HEADER, String.valueOf(0))
                .header(PageRequestVO.TOTAL_ELEMENTS_HEADER, String.valueOf(0))
                .header(PageRequestVO.TOTAL_PAGES_HEADER, String.valueOf(1));
    }

    @Test
    public void givenIdAndVO_whenUpdate_thenReturnStatus200() throws JsonProcessingException {
        ID id = Instancio.create(idClass);
        when(getService().update(any(), any(entityClass))).thenReturn(entityUpdated);
        when(getConverter().convertToEntity(any())).thenReturn(entityUpdated);
        when(getConverter().convertToVO(any())).thenReturn(voUpdated);

        given().contentType(ContentType.JSON).body(voUpdated)
                .when().put(basePath + "/" + id)
                .then().log().ifValidationFails()
                .assertThat().status(HttpStatus.OK)
                .body(equalTo(mapper.writeValueAsString(voUpdated)));
    }

    @Test
    public void givenIdAndVO_whenUpdate_thenReturnStatus400() {
        VO voFailsValidations = Instancio.createBlank(voClass);
        ID id = Instancio.create(idClass);

        given().contentType(ContentType.JSON).body(voFailsValidations)
                .when().put(basePath + "/" + id)
                .then().log().ifValidationFails()
                .assertThat().status(HttpStatus.BAD_REQUEST)
                .body(not(emptyString()))
                .body(containsString("must not be"));

        verify(getConverter(), never()).convertToEntity(any());
        verify(getService(), never()).update(any(), any(entityClass));
        verify(getConverter(), never()).convertToVO(any());
    }

    @Test
    public void givenIdAndVO_whenUpdate_thenReturnStatus404() {
        ID id = Instancio.create(idClass);
        when(getService().update(any(), any(entityClass))).thenThrow(EntityNotFoundException.class);
        when(getConverter().convertToEntity(any())).thenReturn(entityToUpdate);
        when(getConverter().convertToVO(any())).thenReturn(voUpdated);

        given().contentType(ContentType.JSON).body(voUpdated)
                .when().put(basePath + "/" + id)
                .then().log().ifValidationFails()
                .assertThat().status(HttpStatus.NOT_FOUND)
                .body(not(emptyString()));

        verify(getConverter(), never()).convertToVO(any());
    }

    @Test
    public void givenIdAndVO_whenPartialUpdate_thenReturnStatus200() throws JsonProcessingException {
        ID id = Instancio.create(idClass);
        when(getService().update(any(), any(UpdaterExample.class))).thenReturn(entityUpdated);
        when(getConverter().convertToEntity(any())).thenReturn(entityUpdated);
        when(getConverter().convertToVO(any())).thenReturn(voPartialUpdated);

        given().contentType(ContentType.JSON).body(voPartialUpdated)
                .when().patch(basePath + "/" + id)
                .then().log().ifValidationFails()
                .assertThat().status(HttpStatus.OK)
                .body(equalTo(mapper.writeValueAsString(voPartialUpdated)));
    }

    @Test
    public void givenIdAndVO_whenPartialUpdate_thenReturnStatus404() {
        ID id = Instancio.create(idClass);
        when(getService().update(any(), any(UpdaterExample.class))).thenThrow(EntityNotFoundException.class);
        when(getConverter().convertToEntity(any())).thenReturn(entityToUpdate);

        given().contentType(ContentType.JSON).body(voToUpdate)
                .when().patch(basePath + "/" + id)
                .then().log().ifValidationFails()
                .assertThat().status(HttpStatus.NOT_FOUND)
                .body(not(emptyString()));

        verify(getConverter(), never()).convertToVO(any());
    }

    @Test
    public void givenId_whenDelete_thenReturnStatus204() {
        ID id = Instancio.create(idClass);
        doNothing().when(getService()).deleteById(any());

        given().when().delete(basePath + "/" + id)
                .then().log().ifValidationFails()
                .assertThat().status(HttpStatus.NO_CONTENT)
                .body(emptyString());
    }

    @Test
    public void givenId_whenDelete_thenReturnStatus404() {
        ID id = Instancio.create(idClass);
        doThrow(EntityNotFoundException.class).when(getService()).deleteById(any());

        given().when().delete(basePath + "/" + id)
                .then().log().ifValidationFails()
                .assertThat().status(HttpStatus.NOT_FOUND)
                .body(not(emptyString()));
    }

}
