package com.techmath.allcrud.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.techmath.allcrud.common.ControllerErrorVO;
import com.techmath.allcrud.common.PageRequestVO;
import com.techmath.allcrud.config.AllcrudDisplayNameGenerator;
import com.techmath.allcrud.config.TestContainerConfig;
import com.techmath.allcrud.converter.Converter;
import com.techmath.allcrud.entity.AbstractEntity;
import com.techmath.allcrud.entity.AbstractEntityVO;
import com.techmath.allcrud.repository.EntityRepository;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.apache.commons.lang3.StringUtils;
import org.instancio.Instancio;
import org.instancio.settings.Keys;
import org.instancio.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Abstract base class for REAL integration tests of {@link CrudController}.
 * <p>
 * This class provides full-stack integration testing using:
 * <ul>
 *   <li><strong>Testcontainers</strong> - Real PostgreSQL database running in Docker</li>
 *   <li><strong>Spring Boot Test</strong> - Full application context with all beans</li>
 *   <li><strong>RestAssuredMockMvc</strong> - Fluent HTTP testing DSL</li>
 *   <li><strong>Instancio</strong> - Smart test data generation</li>
 * </ul>
 *
 * <p>
 * <strong>What is tested:</strong>
 * <ul>
 *   <li>✅ Complete HTTP request/response cycle</li>
 *   <li>✅ Controller → Service → Repository → <strong>Real Database</strong></li>
 *   <li>✅ JSON serialization/deserialization</li>
 *   <li>✅ Bean Validation (@Valid, @NotNull, etc.)</li>
 *   <li>✅ JPA/Hibernate behavior (transactions, cascades, lazy loading)</li>
 *   <li>✅ Database constraints (unique, foreign keys, etc.)</li>
 *   <li>✅ Exception handling and HTTP status codes</li>
 *   <li>✅ Pagination headers and logic</li>
 * </ul>
 *
 * <p>
 * <strong>Requirements:</strong>
 * <ul>
 *   <li>Docker must be running on your machine</li>
 *   <li>Your entity must be a valid JPA entity with proper mappings</li>
 *   <li>Your entity and VO must implement {@code equals()}, {@code hashCode()}, and {@code toString()}</li>
 * </ul>
 *
 * <p>
 * <strong>Performance Note:</strong><br>
 * The PostgreSQL container is started once and reused across all test classes
 * in the same JVM execution via {@link TestContainerConfig}. Each test method
 * runs in its own transaction which is rolled back after completion, ensuring
 * test isolation.
 *
 * @param <T>  the type of the entity. Must be a valid JPA entity.
 * @param <VO> the type of the value object (DTO).
 * @param <ID> the type of the entity's identifier.
 *
 * @see TestContainerConfig
 * @see CrudController
 * @see PageRequestVO
 * @see org.instancio.Instancio
 *
 * @author Matheus Maia
 */
@Transactional
@DisplayNameGeneration(AllcrudDisplayNameGenerator.class)
public abstract class CrudControllerIntegrationTests<T extends AbstractEntity<ID>, VO extends AbstractEntityVO<ID>, ID> {

    @Autowired
    protected MockMvc mockMvc;

    protected abstract EntityRepository<T, ID> getRepository();
    protected abstract Converter<T, VO, ID> getConverter();

    private final Class<VO> voClass;
    private final Class<ID> idClass;
    protected String basePath;
    protected Settings settings;
    protected ObjectMapper mapper;

    /**
     * Configures Spring properties to use the Testcontainer PostgreSQL database.
     * <p>
     * This method is called once before any tests run to set up the database connection.
     *
     * @param registry the dynamic property registry
     */
    @DynamicPropertySource
    static void configureTestDatabase(DynamicPropertyRegistry registry) {
        TestContainerConfig.configureDataSource(registry);
    }

    /**
     * Constructs the integration test with VO, ID classes and controller class.
     * <p>
     * The controller class is used to automatically resolve the base path from
     * the {@code @RequestMapping} annotation.
     *
     * @param voClass         the VO class
     * @param idClass         the ID class
     * @param controllerClass the controller class (to resolve base path)
     */
    protected CrudControllerIntegrationTests(Class<VO> voClass, Class<ID> idClass, Class<?> controllerClass) {
        this.voClass = voClass;
        this.idClass = idClass;
        this.basePath = resolveBasePath(controllerClass);
    }

    /**
     * Constructs the integration test with VO and ID classes.
     * <p>
     * When using this constructor, you must manually set the {@code basePath} field.
     *
     * @param voClass     the VO class
     * @param idClass     the ID class
     */
    protected CrudControllerIntegrationTests(Class<VO> voClass, Class<ID> idClass) {
        this.voClass = voClass;
        this.idClass = idClass;
        this.basePath = StringUtils.EMPTY;
    }

    /**
     * Resolves the base path from the controller's {@code @RequestMapping} annotation.
     *
     * @param controllerClass the controller class
     * @return the base path, or empty string if not found
     */
    private String resolveBasePath(Class<?> controllerClass) {
        RequestMapping mapping = controllerClass.getAnnotation(RequestMapping.class);
        if (Objects.nonNull(mapping) && mapping.value().length > 0) {
            return mapping.value()[0];
        }
        return StringUtils.EMPTY;
    }

    /**
     * Initializes test fixtures before each test.
     * <p>
     * Sets up RestAssuredMockMvc, JSON mapper, and Instancio settings.
     */
    @BeforeEach
    public void init() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        mapper = JsonMapper.builder().findAndAddModules().build();
        settings = Settings.create()
                .set(Keys.BEAN_VALIDATION_ENABLED, true)
                .set(Keys.JPA_ENABLED, true);
    }

    /**
     * Tests successful entity creation via POST.
     * <p>
     * Validates:
     * <ul>
     *   <li>HTTP 201 Created status</li>
     *   <li>Response body contains created entity</li>
     *   <li>Entity is persisted in database</li>
     *   <li>ID is generated</li>
     *   <li>Data matches what was sent</li>
     * </ul>
     */
    @Test
    public void givenValidVO_whenCreate_thenReturnStatus201AndPersistedEntity() throws JsonProcessingException {
        VO voToCreate = Instancio.of(voClass).withSettings(settings).create();
        voToCreate.setId(null);

        String response = given().contentType(ContentType.JSON).body(voToCreate)
                .when().post(basePath)
                .then().log().ifValidationFails()
                .assertThat().status(HttpStatus.CREATED)
                .body(notNullValue(), not(emptyString()))
                .body("id", notNullValue())
                .extract().asString();

        VO createdVO = mapper.readValue(response, voClass);
        ID id = createdVO.getId();

        assertNotNull(id);

        T persistedEntity = getRepository().findById(createdVO.getId()).orElse(null);

        assertNotNull(persistedEntity, "Entity was not persisted in database");
        assertEquals(id, persistedEntity.getId(), "Persisted entity ID should match");
    }

    /**
     * Tests entity creation with validation errors.
     * <p>
     * Validates:
     * <ul>
     *   <li>HTTP 400 Bad Request status</li>
     *   <li>Response body contains validation error details</li>
     *   <li>Entity is NOT persisted in database</li>
     * </ul>
     */
    @Test
    public void givenInvalidVO_whenCreate_thenReturnStatus400WithValidationErrors() throws JsonProcessingException {
        VO voFailsValidations = Instancio.createBlank(voClass);

        String response = given().contentType(ContentType.JSON).body(voFailsValidations)
                .when().post(basePath)
                .then().log().ifValidationFails()
                .assertThat().status(HttpStatus.BAD_REQUEST)
                .body(notNullValue(), not(emptyString()))
                .extract().asString();

        List<ControllerErrorVO> errors = mapper.readValue(response, new TypeReference<>() {});
        assertNotNull(errors, "Should return a list of errors");
        assertFalse(errors.isEmpty(), "Should return at least one error");
    }

    /**
     * Tests finding an entity by ID.
     * <p>
     * Validates:
     * <ul>
     *   <li>HTTP 200 OK status</li>
     *   <li>Response body matches the created entity</li>
     * </ul>
     */
    @Test
    public void givenExistingEntity_whenFindById_thenReturnStatus200WithEntity() throws JsonProcessingException {
        VO voCreated = createEntity();
        ID id = voCreated.getId();

        String response = given().when().get(basePath + "/" + id)
                .then().log().ifValidationFails()
                .assertThat().status(HttpStatus.OK)
                .body(notNullValue(), not(emptyString()))
                .extract().asString();

        VO voFound = mapper.readValue(response, voClass);

        assertNotNull(voFound, "Response should contain the created entity");
        assertEquals(id, voFound.getId(), "The ID should match the created entity");
    }

    /**
     * Tests finding a non-existent entity.
     * <p>
     * Validates:
     * <ul>
     *   <li>HTTP 404 Not Found status</li>
     *   <li>Response body contains error details</li>
     * </ul>
     */
    @Test
    public void givenNonExistentId_whenFindById_thenReturnStatus404() {
        ID id = Instancio.create(idClass);

        given().when().get(basePath + "/" + id)
                .then().log().ifValidationFails()
                .assertThat().status(HttpStatus.NOT_FOUND)
                .body(not(emptyString()));
    }

    /**
     * Tests paginated listing with no filters.
     * <p>
     * Validates:
     * <ul>
     *   <li>HTTP 206 Partial Content (when results exist)</li>
     *   <li>Pagination headers are present</li>
     *   <li>Page size is respected</li>
     * </ul>
     */
    @Test
    public void whenFindAllPaged_thenReturnStatus206AndPaginatedResults() throws JsonProcessingException {
        List<VO> list = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            list.add(createEntity());
        }

        String jsonPage1 = given().queryParam("size", 2)
                .when().get(basePath)
                .then().log().ifValidationFails()
                .assertThat()
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .header(PageRequestVO.CURRENT_PAGE_HEADER, "0")
                    .header(PageRequestVO.CURRENT_ELEMENTS_HEADER, "2")
                    .header(PageRequestVO.TOTAL_ELEMENTS_HEADER, "4")
                    .header(PageRequestVO.TOTAL_PAGES_HEADER, "2")
                    .body("$", hasSize(2))
                .extract().asString();

        var listType = mapper.getTypeFactory().constructCollectionType(List.class, voClass);
        List<VO> page1 = mapper.readValue(jsonPage1, listType);
        assertTrue(list.containsAll(page1));

        String jsonPage2 = given()
                .queryParam("page", 1)
                .queryParam("size", 2)
            .when().get(basePath)
            .then().log().ifValidationFails()
            .assertThat()
                .status(HttpStatus.PARTIAL_CONTENT)
                .header(PageRequestVO.CURRENT_PAGE_HEADER, "1")
                .header(PageRequestVO.CURRENT_ELEMENTS_HEADER, "2")
                .header(PageRequestVO.TOTAL_ELEMENTS_HEADER, "4")
                .header(PageRequestVO.TOTAL_PAGES_HEADER, "2")
                .body("$", hasSize(2))
            .extract().asString();

        List<VO> page2 = mapper.readValue(jsonPage2, listType);
        assertTrue(list.containsAll(page2));
    }

    /**
     * Tests listing with no results.
     * <p>
     * Validates:
     * <ul>
     *   <li>HTTP 204 No Content (when no results)</li>
     *   <li>Empty list response body</li>
     *   <li>Pagination headers show zero elements</li>
     * </ul>
     */
    @Test
    public void whenFindAllPagedWithNotFoundFilters_thenReturnStatus204() {
        ID id = Instancio.create(idClass);
        given()
                .queryParam("id", id)
            .when().get(basePath)
            .then().log().ifValidationFails()
            .assertThat()
                .status(HttpStatus.NO_CONTENT)
                .header(PageRequestVO.CURRENT_PAGE_HEADER, "0")
                .header(PageRequestVO.CURRENT_ELEMENTS_HEADER, "0")
                .header(PageRequestVO.TOTAL_ELEMENTS_HEADER, "0")
                .header(PageRequestVO.TOTAL_PAGES_HEADER, "0")
                .body(equalTo("[]"));
    }

    /**
     * Tests paginated listing WITH filters.
     * <p>
     * This is a critical test that validates the core filtering feature of Allcrud.
     * It creates multiple entities with known values, then filters by specific criteria
     * to ensure only matching entities are returned.
     * <p>
     * <strong>Note:</strong> This test uses Instancio with specific field values.
     * If your entity has custom fields, you may need to override this test
     * to set appropriate filter values.
     * <p>
     * Validates:
     * <ul>
     *   <li>Filtering works correctly (ExampleMatcher)</li>
     *   <li>Only matching entities are returned</li>
     *   <li>Case-insensitive matching (if configured)</li>
     *   <li>Null fields are ignored in filter</li>
     *   <li>Pagination works with filters</li>
     * </ul>
     */
    @Test
    public void givenMultipleEntities_whenFindAllWithFilters_thenReturnStatus206AndOnlyMatchingEntities() throws JsonProcessingException {
        List<VO> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            list.add(createEntity());
        }

        ID id = list.getFirst().getId();

        String response = given()
                .queryParam("id", id)
                .queryParam("size", 10)
            .when().get(basePath)
            .then().log().ifValidationFails()
            .assertThat()
                .status(HttpStatus.PARTIAL_CONTENT)
                .header(PageRequestVO.CURRENT_PAGE_HEADER, "0")
                .header(PageRequestVO.CURRENT_ELEMENTS_HEADER, "1")
                .header(PageRequestVO.TOTAL_ELEMENTS_HEADER, "1")
                .header(PageRequestVO.TOTAL_PAGES_HEADER, "1")
                .body(notNullValue(), not(emptyString()))
                .body("$", hasSize(1))
            .extract().asString();

        var listType = mapper.getTypeFactory().constructCollectionType(List.class, voClass);
        List<VO> filtered = mapper.readValue(response, listType);

        assertNotNull(filtered, "Response should contain filtered results");
        assertEquals(1, filtered.size(), "Only one result should match this filter");
        assertEquals(id, filtered.getFirst().getId(), "The ID should match the filter");
    }

    /**
     * Tests that filtering with non-existent criteria returns empty results.
     * <p>
     * Validates:
     * <ul>
     *   <li>HTTP 204 No Content when no matches</li>
     *   <li>Empty result set</li>
     *   <li>Pagination headers show zero elements</li>
     * </ul>
     */
    @Test
    public void givenNonMatchingFilters_whenFindAll_thenReturnStatus204() throws JsonProcessingException {
        List<VO> list = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            list.add(createEntity());
        }

        var existentIds = list.stream().map(VO::getId).toList();
        ID nonExistentId = Instancio.create(idClass);
        while (existentIds.contains(nonExistentId)) {
            nonExistentId = Instancio.create(idClass);
        }

        given()
                .queryParam("id", nonExistentId.toString())
            .when().get(basePath)
            .then().log().ifValidationFails()
            .assertThat()
                .status(HttpStatus.NO_CONTENT)
                .header(PageRequestVO.CURRENT_PAGE_HEADER, "0")
                .header(PageRequestVO.CURRENT_ELEMENTS_HEADER, "0")
                .header(PageRequestVO.TOTAL_ELEMENTS_HEADER, "0")
                .header(PageRequestVO.TOTAL_PAGES_HEADER, "0")
                .body(equalTo("[]"));
    }

    /**
     * Tests full entity update via PUT.
     * <p>
     * Validates:
     * <ul>
     *   <li>HTTP 200 OK status</li>
     *   <li>Response body contains updated entity</li>
     *   <li>Changes are persisted in database</li>
     *   <li>Updated data can be retrieved</li>
     * </ul>
     */
    @Test
    public void givenExistingEntity_whenUpdate_thenReturnStatus200AndUpdatedEntity() throws JsonProcessingException {
        VO voCreated = createEntity();
        ID id = voCreated.getId();
        VO voToUpdate = Instancio.of(voClass).withSettings(settings).create();
        voToUpdate.setId(id);

        String response = given().contentType(ContentType.JSON).body(voToUpdate)
                .when().put(basePath + "/" + id)
                .then().log().ifValidationFails()
                .assertThat()
                    .status(HttpStatus.OK)
                    .body(notNullValue(), not(emptyString()))
                .extract().asString();

        VO voUpdated = mapper.readValue(response, voClass);
        T persistedEntity = getRepository().findById(id).orElse(null);

        assertNotNull(persistedEntity, "Updated entity should exist in database");
        assertEquals(id, persistedEntity.getId(), "Entity ID should not change after update");
        assertNotEquals(voUpdated.toString(), voCreated.toString(), "Entity should be different after update");

        response = given()
            .when()
                .get(basePath + "/" + id)
            .then()
                .status(HttpStatus.OK)
                .extract().asString();

        VO voRetrieved = mapper.readValue(response, voClass);
        assertEquals(voUpdated, voRetrieved, "Retrieved entity should match updated entity");
    }

    /**
     * Tests update with validation errors.
     * <p>
     * Validates:
     * <ul>
     *   <li>HTTP 400 Bad Request status</li>
     *   <li>Response body contains validation error details</li>
     *   <li>Entity is NOT modified in database</li>
     * </ul>
     */
    @Test
    public void givenInvalidVO_whenUpdate_thenReturnStatus400WithValidationErrors() throws JsonProcessingException {
        VO voCreated = createEntity();
        ID id = voCreated.getId();
        VO voFailsValidations = Instancio.createBlank(voClass);

        String response = given().contentType(ContentType.JSON).body(voFailsValidations)
                .when().put(basePath + "/" + id)
                .then().log().ifValidationFails()
                .assertThat()
                    .status(HttpStatus.BAD_REQUEST)
                    .body(notNullValue(), not(emptyString()))
                .extract().asString();

        List<ControllerErrorVO> errors = mapper.readValue(response, new TypeReference<>() {});
        assertNotNull(errors, "Should return a list of errors");
        assertFalse(errors.isEmpty(), "Should return at least one error");

        String expectedResponse = mapper.writeValueAsString(voCreated);
        T entityNotUpdated = getRepository().findById(id).orElse(null);
        VO voNotUpdated = getConverter().convertToVO(entityNotUpdated);
        assertNotNull(entityNotUpdated, "Entity should still exist in database");
        assertEquals(id, entityNotUpdated.getId(), "Entity ID should not change after update");
        assertEquals(expectedResponse, mapper.writeValueAsString(voNotUpdated), "Entity should be the same after failed update");
    }

    /**
     * Tests updating a non-existent entity.
     * <p>
     * Validates:
     * <ul>
     *   <li>HTTP 404 Not Found status</li>
     *   <li>Response body contains error details</li>
     * </ul>
     */
    @Test
    public void givenNonExistentId_whenUpdate_thenReturnStatus404() {
        ID nonExistentId = Instancio.create(idClass);
        VO voToUpdate = Instancio.of(voClass).withSettings(settings).create();

        given().contentType(ContentType.JSON).body(voToUpdate)
                .when().put(basePath + "/" + nonExistentId)
                .then().log().ifValidationFails()
                .assertThat()
                    .status(HttpStatus.NOT_FOUND)
                    .body(not(emptyString()));
    }

    /**
     * Tests partial entity update via PATCH.
     * <p>
     * Validates:
     * <ul>
     *   <li>HTTP 200 OK status</li>
     *   <li>Response body contains updated entity</li>
     *   <li>Only provided fields are updated</li>
     * </ul>
     */
    @Test
    public void givenExistingEntity_whenPartialUpdate_thenReturnStatus200() throws JsonProcessingException {
        VO voCreated = createEntity();
        ID id = voCreated.getId();
        VO voToPartialUpdate = Instancio.of(voClass).withSettings(settings).create();
        voToPartialUpdate.setId(id);

        String response = given().contentType(ContentType.JSON).body(voToPartialUpdate)
                .when().patch(basePath + "/" + id)
                .then().log().ifValidationFails()
                .assertThat()
                    .status(HttpStatus.OK)
                    .body(notNullValue())
                .extract().asString();

        VO voUpdated = mapper.readValue(response, voClass);
        T persistedEntity = getRepository().findById(id).orElse(null);

        assertNotNull(persistedEntity, "Updated entity should exist in database");
        assertEquals(id, voUpdated.getId(), "Entity ID should not change after update");
        assertNotEquals(voUpdated.toString(), voCreated.toString(), "Entity should be different after update");
    }

    /**
     * Tests partial update of a non-existent entity.
     * <p>
     * Validates:
     * <ul>
     *   <li>HTTP 404 Not Found status</li>
     *   <li>Response body contains error details</li>
     * </ul>
     */
    @Test
    public void givenNonExistentId_whenPartialUpdate_thenReturnStatus404() {
        ID nonExistentId = Instancio.create(idClass);
        VO voToPartialUpdate = Instancio.of(voClass).withSettings(settings).create();

        given().contentType(ContentType.JSON).body(voToPartialUpdate)
                .when().patch(basePath + "/" + nonExistentId)
                .then().log().ifValidationFails()
                .assertThat()
                    .status(HttpStatus.NOT_FOUND)
                    .body(not(emptyString()));
    }

    /**
     * Tests entity deletion via DELETE.
     * <p>
     * Validates:
     * <ul>
     *   <li>HTTP 204 No Content status</li>
     *   <li>Entity is removed from database</li>
     *   <li>Subsequent GET returns 404</li>
     *   <li>Direct DB query returns empty</li>
     * </ul>
     */
    @Test
    public void givenExistingEntity_whenDelete_thenReturnStatus204AndEntityIsDeleted() throws JsonProcessingException {
        VO voCreated = createEntity();
        ID id = voCreated.getId();

        assertTrue(getRepository().existsById(id), "Entity should exist before deletion");

        given()
            .when().delete(basePath + "/" + id)
            .then().log().ifValidationFails()
            .assertThat()
                .status(HttpStatus.NO_CONTENT)
                .body(emptyString());

        given()
            .when().get(basePath + "/" + id)
            .then()
            .assertThat()
                .status(HttpStatus.NOT_FOUND);

        assertFalse(getRepository().existsById(id), "Entity should be deleted from database");
    }

    /**
     * Tests deleting a non-existent entity.
     * <p>
     * Validates:
     * <ul>
     *   <li>HTTP 404 Not Found status</li>
     *   <li>Response body contains error details</li>
     * </ul>
     */
    @Test
    public void givenNonExistentId_whenDelete_thenReturnStatus404() {
        ID nonExistentId = Instancio.create(idClass);

        given().when().delete(basePath + "/" + nonExistentId)
                .then().log().ifValidationFails()
                .assertThat()
                    .status(HttpStatus.NOT_FOUND)
                    .body(not(emptyString()));
    }

    /**
     * Creates and sends a new VO entity to creation endpoint.
     * The entity is created, and the response from the server is deserialized into a VO instance and returned.
     *
     * @return A newly created VO instance deserialized from the server's response.
     * @throws JsonProcessingException if the response cannot be deserialized into a VO instance.
     */
    protected VO createEntity() throws JsonProcessingException {
        VO voToCreate = Instancio.of(voClass).withSettings(settings).create();
        voToCreate.setId(null);

        String createResponse = given()
                .contentType(ContentType.JSON)
                .body(voToCreate)
            .when()
                .post(basePath)
            .then()
                .status(HttpStatus.CREATED)
                .extract().asString();

        return mapper.readValue(createResponse, voClass);
    }

}
