# Allcrud

---
**Allcrud** is a Java library designed to accelerate the development of reusable **REST CRUD APIs with Spring Boot**. With a generic and extensible structure aligned to **RESTful** principles, it enables developers to implement complete endpoints quickly, while following best practices and clean architecture guidelines.

To top it off, Allcrud also provides a robust base for unit and integration testing — fully prepared to scale with your project.

Stop wasting time writing the same CRUD logic over and over again. Import, extend, and deliver.

---

## ✨ Features

- Ready-to-use generic Controllers, Services, and Repositories
- Native pagination support
- Clean separation between Entities and VOs (DTOs) via a flexible converter system
- Centralized exception handling
- Full support for business rule exceptions mapped to **HTTP 422**
- Built-in validation and data utilities
- Support for auditable entities
- Support for dynamic filtering
- Support for soft delete
- Base structure for unit and integration tests via test fixtures


---

## 📦 Getting Started

Here is a quick teaser of an application using **Allcrud** in Java:

### 🔍 Example: Entity `Product`

```java
@Entity
public class Product extends AbstractEntity {
    private String name;
    private BigDecimal price;
    
    // getters and setters
}

public class ProductVO implements AbstractEntityVO {
    private String name;
    private BigDecimal price;
    
    // getters and setters
}

@Component
public class ProductConverter implements Converter<Product, ProductVO> {
    
    @Override
    public ProductVO convertToVO(Product entity) {
        ProductVO vo = new ProductVO();
        vo.setName(entity.getName());
        vo.setPrice(entity.getPrice());
        return vo;
    }

    @Override
    public Product convertToEntity(ProductVO vo) {
        Product entity = new Product();
        entity.setName(vo.getName());
        entity.setPrice(vo.getPrice());
        return entity;
    }
}

@Repository
public interface ProductRepository extends EntityRepository<Product> {}

@Service
public class ProductService extends CrudService<Product> {
    
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    protected EntityRepository<Product> getRepository() {
        return repository;
    }

    // Your custom methods here
    // Note: All CRUD methods are already implemented by CrudService
}

@RestController
@RequestMapping("/product")
public class ProductController extends CrudController<Product, ProductVO> {
    
    private final ProductService service;
    private final ProductConverter converter;

    public ProductController(ProductService service, ProductConverter converter) {
        this.service = service;
        this.converter = converter;
    }

    @Override
    protected CrudService<Product> getService() {
        return service;
    }

    @Override
    protected Converter<Product, ProductVO> getConverter() {
        return converter;
    }

    // Your custom endpoints here
    // Note: All CRUD endpoints are already implemented by CrudController
}
```

> Allcrud supports dynamic filtering by passing a VO as query parameters.  
> These values are converted to an entity and used for example-based filtering (Spring Data's `ExampleMatcher`).

> 💡 Need more control?  
> Allcrud allows you to **override any controller or service method** to customize behavior — like applying validation groups, adding business logic, or defining custom endpoints. Just extend and override.

### Gradle/Maven Configuration

Add the Gradle/Maven dependency:

### Gradle
```groovy
dependencies {
    implementation 'com.techmath:allcrud:0.1.0'
    testImplementation testFixtures('com.techmath:allcrud:0.1.0')
}
```

### Maven
```xml
<!-- Main library -->
<dependency>
    <groupId>com.techmath</groupId>
    <artifactId>allcrud</artifactId>
    <version>0.1.0</version>
</dependency>

<!-- Test fixtures support -->
<dependency>
    <groupId>com.techmath</groupId>
    <artifactId>allcrud</artifactId>
    <version>0.1.0</version>
    <classifier>test-fixtures</classifier>
    <scope>test</scope>
</dependency>
```
---
## 📌 Design Decisions

Allcrud is built with flexibility and minimalism in mind. Below are some intentional design choices made during development:

- ✅ **Manual or automated conversion**: The `Converter<T, VO>` interface supports both manual mapping and tools like MapStruct or ModelMapper — your choice.
- ✅ **Validation logic belongs to the developer**: We don't enforce validation groups or flow-specific behavior (like OnCreate vs OnUpdate), but you can override methods and apply them yourself.
- ✅ **Soft delete is opt-in**: If your entity implements `SoftDeletable`, Allcrud will call `softDelete()` — you define what it means to "soft delete".
- ✅ **Filtering is done via VO**: Instead of creating a complex query language, we leverage Spring Data’s `ExampleMatcher` using converted VOs as filter inputs.
- ❌ **No child entity abstractions**: Relationships like `1:N` (e.g., users → addresses) are highly domain-specific. We encourage developers to implement them using standard Spring patterns.

> All of these decisions aim to keep Allcrud powerful, but never prescriptive. You’re always in control.

---
## 🧪 Testing Support

Allcrud includes a reusable and extensible test infrastructure for both **unit** and **integration** testing.

You can extend the provided abstract test classes to easily test your own services, controllers, and integration flows with minimal boilerplate — using `Instancio`, `Mockito`, and `Spring Test` under the hood.

### ✅ Base Classes Available

- `CrudServiceTests`
- `CrudControllerTests`
- `CrudIntegrationTests`

Each abstract class provides built-in logic for CRUD operations, which you can extend and specialize for your own domain objects.
> 📌 Integration tests are powered by `RestAssuredMockMvc`, and also fully support **Java Bean Validation** (e.g. `@NotNull`, `@Size`, etc.). Validation is enabled by default, but can be turned off by setting `BEAN_VALIDATION_ENABLED` to `false` in Instancio `Settings`.

> 📌 Bonus: `CrudIntegrationTests` will automatically detect the controller's `@RequestMapping` base path by reflection.  
> Just pass the controller class as the **third constructor parameter**.  
> Prefer manual control? You can also set the protected `basePath` field directly.
---

### 🔍 Example: Testing `Product`

With Allcrud, testing your CRUD layers is not an afterthought — it's built-in. Just extend, inject, and assert.

```java
@ExtendWith(MockitoExtension.class)
public class ProductServiceTests extends CrudServiceTests<Product> {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    public ProductServiceTests() {
        super(Product.class);
    }

    @Override
    protected EntityRepository<Product> getRepository() {
        return repository;
    }

    @Override
    protected CrudService<Product> getService() {
        return service;
    }

    // Your custom tests here
    // Note: All CRUD tests are already implemented by CrudServiceTests
}
```
```java
@ExtendWith(MockitoExtension.class)
public class ProductControllerTests extends CrudControllerTests<Product, ProductVO> {

    @Mock
    private ProductService service;

    @Mock
    private ProductConverter converter;

    private final ProductController controller = new ProductController(service, converter);

    public ProductControllerTests() {
        super(Product.class, ProductVO.class);
    }

    @Override
    protected CrudService<Product> getService() {
        return service;
    }

    @Override
    protected Converter<Product, ProductVO> getConverter() {
        return converter;
    }

    @Override
    protected CrudController<Product, ProductVO> getController() {
        return controller;
    }

    // Your custom tests here
    // Note: All CRUD tests are already implemented by CrudControllerTests
}
```
```java
@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc
public class ProductIntegrationTests extends CrudIntegrationTests<Product, ProductVO> {

    @MockBean
    private ProductService service;

    @MockBean
    private ProductConverter converter;

    public ProductIntegrationTests() {
        super(Product.class, ProductVO.class);
        basePath = "/product";
    }

    @Override
    protected CrudService<Product, ProductVO> getService() {
        return service;
    }

    @Override
    protected Converter<Product, ProductVO> getConverter() {
        return converter;
    }
    
    // Your custom tests here
    // Note: All CRUD tests are already implemented by CrudIntegrationTests
}
```
> If you work with @RequestMapping at class level, you can use this constructor instead:

```java
public ProductIntegrationTests() {
    super(Product.class, ProductVO.class, ProductController.class);
}
```
---
## 📄 License

This project is licensed under the [MIT License](LICENSE).  
Feel free to use, modify, and distribute it with attribution.

## 🤝 Contributors

Thanks to everyone who has contributed to this project! 💙

Want to contribute? Feel free to open issues, suggest enhancements, or submit pull requests.


## 💬 Contact

For questions, suggestions or feedback, open an issue or contact **mathmferreira@gmail.com**.

---