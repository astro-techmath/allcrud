# Allcrud
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring_boot-%236DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white)

---
### 💖 Support This Project

This project is open source and free to use.  
If you find it helpful, consider supporting its continued development:

[![Donate](https://img.shields.io/badge/PayPal_USD-00457C?style=for-the-badge&logo=paypal&logoColor=white)](https://www.paypal.com/donate/?business=KZVGXBKSZLC6W&no_recurring=0&item_name=Support+Allcrud+and+help+bring+powerful%2C+clean+tools+to+passionate+developers+around+the+world.+%F0%9F%92%99%F0%9F%9A%80%0A&currency_code=USD)
[![Donate](https://img.shields.io/badge/PayPal_BRL-65af00?style=for-the-badge&logo=paypal&logoColor=white)](https://www.paypal.com/donate/?business=KZVGXBKSZLC6W&no_recurring=0&item_name=Support+Allcrud+and+help+bring+powerful%2C+clean+tools+to+passionate+developers+around+the+world.+%F0%9F%92%99%F0%9F%9A%80%0A&currency_code=BRL)
![Donate](https://img.shields.io/badge/PIX-4BB7A8?style=for-the-badge&logo=pix&logoColor=white)

> 💡 For Brazilian PIX donation, use the key: **mathmferreira@gmail.com**. You can also use the QR code [here](qrcode-pix.png).

---
## 📃 About
**Allcrud** is a Java library designed to accelerate the development of reusable **REST CRUD APIs with Spring Boot**. With a generic and extensible structure aligned to **RESTful** principles, it enables developers to implement complete endpoints quickly, while following best practices and clean architecture guidelines.

To top it off, Allcrud also provides a robust base for unit and integration testing — fully prepared to scale with your project.

Stop wasting time writing the same CRUD logic over and over again. Import, extend, and deliver.

> 💡 This project is built in **Java 21** and **Spring Boot 3.5.0**
---

## ✨ Features

- Ready-to-use generic Controllers, Services, and Repositories
- Support for any ID type (Long, UUID, String, composite keys, etc.)
- Native pagination support
- Clean separation between Entities and VOs (DTOs) via a flexible converter system
- Centralized exception handling
- Full support for business rule exceptions mapped to **HTTP 422** (customizable)
- Built-in validation and data utilities
- Support for auditable entities
- Support for dynamic filtering via **Spring Data Example**
- Base structure for unit and integration tests via test fixtures


---

## 📚 Libraries Used

Allcrud relies on a few powerful open-source libraries to enhance the testing and development experience:

- [**Instancio**](https://www.instancio.org/) – Smart data generator used in test fixtures for unit and integration tests.
- [**Mockito**](https://site.mockito.org/) – Mocking framework for clean and isolated unit testing.
- [**RestAssured MockMvc**](https://rest-assured.io/) – Fluent and expressive tool for validating REST APIs in integration tests.
- [**Apache Commons Lang 3**](https://commons.apache.org/proper/commons-lang/) – Utility functions for strings, objects, and general-purpose helpers.
- [**Apache Commons Collections 4**](https://commons.apache.org/proper/commons-collections/) – Enhancements and extensions for Java Collections framework.
- [**Lombok**](https://projectlombok.org/) – Reduces boilerplate in Java classes with annotations like `@Getter`, `@Builder`, and more.

> Allcrud is built on top of the Spring Boot ecosystem, but remains flexible and non-intrusive.

---

## 📦 Getting Started

### Gradle/Maven Configuration

Add the Gradle/Maven dependency:

### Gradle
```groovy
dependencies {
    implementation 'com.techmath:allcrud:0.1.0-beta'
    testImplementation testFixtures('com.techmath:allcrud:0.1.0-beta')
}
```

### Maven
```xml
<!-- Main library -->
<dependency>
    <groupId>com.techmath</groupId>
    <artifactId>allcrud</artifactId>
    <version>0.1.0-beta</version>
</dependency>

<!-- Test fixtures support -->
<dependency>
    <groupId>com.techmath</groupId>
    <artifactId>allcrud</artifactId>
    <version>0.1.0-beta</version>
    <classifier>test-fixtures</classifier>
    <scope>test</scope>
</dependency>
```

### ⚙️ Configuration
Allcrud provides a number of interfaces and abstract classes. You will need to use each of them for CRUD flows to work properly.

### ✅ Base Classes Available

- `AbstractEntity<ID>` - with id property already included, **supports any ID type**
- `AuditableEntity<ID>` - with Spring's **AuditingEntityListener**
- `Converter<T, VO, ID>` - for Entity-VO conversion
- `AbstractGlobalExceptionHandler` - providing a default treatment for CRUD common exceptions
- `BusinessException` - a RuntimeException that works with `AbstractGlobalExceptionHandler`
- `EntityRepository<T, ID>` - extends Spring's **JpaRepository** for allcrud's base CRUD operations
- `CrudService<T, ID>`
- `CrudController<T, VO, ID>`

Here is a quick teaser of an application using **Allcrud** in Java:

### 🔍 Example for Entity `Product` with Long ID

### Entity:
```java
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode @ToString
public class Product implements AbstractEntity<Long> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private BigDecimal price;
}
```

### Value Object (VO):
```java
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode @ToString
public class ProductVO implements AbstractEntityVO<Long> {
    private Long id;
    private String name;
    private BigDecimal price;
}
```

> If you prefer the DTO terminology, implement the **AbstractEntityDTO** interface to maintain the semantics.

### 🆔 Using Different ID Types

Allcrud supports **any ID type**. Here are some common examples:

#### Example with UUID:
```java
@Entity
public class User implements AbstractEntity<UUID> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String email;
    
    @Override
    public UUID getId() {
        return id;
    }
    
    @Override
    public void setId(UUID id) {
        this.id = id;
    }
    
    // other fields, getters, setters, equals, hashCode, toString
}
```

#### Example with String:
```java
@Entity
public class Order implements AbstractEntity<String> {
    
    @Id
    private String id; // Could be a custom order number like "ORD-2025-0001"
    
    private LocalDateTime orderDate;
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public void setId(String id) {
        this.id = id;
    }
    
    // other fields, getters, setters, equals, hashCode, toString
}
```

#### Example with composite key:
```java
@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class OrderItemId implements Serializable {
    private Long orderId;
    private Long productId;
}

@Entity
public class OrderItem implements AbstractEntity<OrderItemId> {
    
    @EmbeddedId
    private OrderItemId id;
    
    private Integer quantity;
    
    @Override
    public OrderItemId getId() {
        return id;
    }
    
    @Override
    public void setId(OrderItemId id) {
        this.id = id;
    }
    
    // other fields, getters, setters, equals, hashCode, toString
}
```

### Create the converter class:
```java
@Component
public class ProductConverter implements Converter<Product, ProductVO, Long> {
    
    @Override
    public ProductVO convertToVO(Product entity) {
        ProductVO vo = new ProductVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setPrice(entity.getPrice());
        return vo;
    }

    @Override
    public Product convertToEntity(ProductVO vo) {
        Product entity = new Product();
        entity.setId(vo.getId());
        entity.setName(vo.getName());
        entity.setPrice(vo.getPrice());
        return entity;
    }
    
}
```

### Create your custom global exception handler:
```java
@ControllerAdvice
public class CustomGlobalExceptionHandler extends AbstractGlobalExceptionHandler {
    // Your custom exception handlers here
    // Note: All CRUD common exception handlers are already implemented by AbstractGlobalExceptionHandler
}
```

### Repository:
```java
@Repository
public interface ProductRepository extends EntityRepository<Product, Long> {
}
```

### Service:
```java
@Service
public class ProductService extends CrudService<Product, Long> {
    
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    protected EntityRepository<Product, Long> getRepository() {
        return repository;
    }

    // Your custom methods here
    // Note: All CRUD methods are already implemented by CrudService
}
```

### Controller:
```java
@RestController
@RequestMapping("/product")
public class ProductController extends CrudController<Product, ProductVO, Long> {
    
    private final ProductService service;
    private final ProductConverter converter;

    public ProductController(ProductService service, ProductConverter converter) {
        this.service = service;
        this.converter = converter;
    }

    @Override
    protected CrudService<Product, Long> getService() {
        return service;
    }

    @Override
    protected Converter<Product, ProductVO, Long> getConverter() {
        return converter;
    }

    // Your custom endpoints here
    // Note: All CRUD endpoints are already implemented by CrudController
}
```

> 💡 Need more control?  
> Allcrud allows you to **override any controller or service method** to customize behavior — like applying validation groups, adding business logic, or defining custom endpoints. Just extend and override.

### 🔄 Pagination Support

Allcrud makes it easy to handle pagination by default through the `findAll` method in the base `CrudController`.

This endpoint accepts query parameters for pagination, sorting, and filtering — all passed as URL parameters. Filters are automatically mapped into the VO (Value Object), making it seamless to filter data based on your domain.

#### 📥 Query Parameters

| Parameter   | Type   | Description                                       | Default |
|-------------|--------|---------------------------------------------------|---------|
| `page`      | int    | Page index (zero-based)                           | `0`     |
| `size`      | int    | Number of items per page                          | `20`    |
| `direction` | string | Sorting direction (`ASC` or `DESC`)               | ASC     |
| `orderBy`   | string | Field to sort by                                  | `id`    |
| *others*    | mixed  | Any field from your VO class (used for filtering) | -       |

You can filter results by simply adding query parameters that match fields in your VO — Allcrud takes care of the mapping behind the scenes.
> The VO fields are converted to an entity and used for example-based filtering (Spring Data's `ExampleMatcher`).

#### 📤 Response Behavior

- Returns a list of VO objects in the response body
- Adds pagination metadata in the HTTP response headers as: `currentPage`, `currentElements`, `totalElements` and `totalPages`.
- Returns:
    - `206 Partial Content` when results are found
    - `204 No Content` when no data matches the filters

#### ✅ Example
```
GET /product?page=0&size=10&orderBy=name&direction=ASC&price=50
```

---
## 📌 Design Decisions

Allcrud is built with flexibility and minimalism in mind. Below are some intentional design choices made during development:

- ✅ **Generic ID support**: Use any ID type (Long, UUID, String, composite keys, etc.) - full flexibility for your domain
- ✅ **Manual or automated conversion**: The `Converter<T, VO, ID>` interface supports both manual mapping and tools like MapStruct or ModelMapper — your choice.
- ✅ **Validation logic belongs to the developer**: We don't enforce validation groups or flow-specific behavior (like OnCreate vs OnUpdate), but you can override methods and apply them yourself.
- ✅ **Filtering is done via VO**: Instead of creating a complex query language, we leverage Spring Data's `ExampleMatcher` using converted VOs as filter inputs.
- ❌ **No child entity abstractions**: Relationships like `1:N` (e.g., users → addresses) are highly domain-specific. We encourage developers to implement them using standard Spring patterns.

> All of these decisions aim to keep Allcrud powerful, but never prescriptive. You're always in control.

---
## 🧪 Testing Support

Allcrud includes a reusable and extensible test infrastructure for both **unit** and **integration** testing.

You can extend the provided abstract test classes to easily test your own services, controllers, and integration flows with minimal boilerplate — using `Instancio`, `Mockito`, and `Spring Test` under the hood.

### ✅ Base Test Classes Available

- `CrudServiceTests<T, ID>` - Unit tests for services with mocked repository
- `CrudControllerIntegrationTests<T, VO, ID>` - Full-stack integration tests with real PostgreSQL database

Each abstract class provides built-in logic for CRUD operations, which you can extend and specialize for your own domain objects.

### 🐳 Integration Tests with Testcontainers
The `CrudControllerIntegrationTests` class provides real integration testing using:

- **Testcontainers** - Spins up a real PostgreSQL database in Docker
- **RestAssuredMockMvc** - Fluent HTTP testing DSL
- **Instancio** - Smart test data generation

You must annotate your test class with `@SpringBootTest` and `@AutoConfigureMockMvc` to enable integration testing.
Also, you should anotate your test class with `@Transactional` to rollback database changes after each test.`

What gets tested:

- ✅ Complete HTTP request/response cycle
- ✅ Controller → Service → Repository → Real Database
- ✅ JSON serialization/deserialization
- ✅ Bean Validation (@Valid, @NotNull, etc.)
- ✅ JPA/Hibernate behavior (transactions, cascades, lazy loading)
- ✅ Database constraints (unique, foreign keys, etc.)
- ✅ Exception handling and HTTP status codes
- ✅ Pagination headers and filtering logic

Requirements:

- Docker must be running on your machine
- Entity must be a valid JPA entity with proper mappings
- Entity and VO must implement equals(), hashCode(), and toString()

> 📌 Integration tests have full support for **Java Bean Validation** (e.g. `@NotNull`, `@Size`, etc.). Validation is enabled by default, but can be turned off by setting `BEAN_VALIDATION_ENABLED` to `false` in **Instancio** `Settings`.

> 📌 `CrudControllerIntegrationTests ` will automatically detect the controller's `@RequestMapping` base path by reflection. Just pass the controller class as the **fourth constructor parameter**.  
> Prefer manual control? You can also set the protected `basePath` field directly.

### ⚙️ Requirements for Built-in Test Support

To make the most of Allcrud's built-in test infrastructure, there are a couple of important things to keep in mind when setting up your Entities and VOs:

#### 🔁 Implement `equals`, `hashCode` and `toString`

Your **Entities** and **VOs** should implement the `equals()`, `hashCode()` and `toString()` methods properly.

These methods are used in the test assertions to accurately compare objects and generate meaningful error messages when something doesn't match. Without them, some tests might fail unexpectedly or return unreliable results.

#### 🛡️ Custom Controller Advice for Integration Tests

For integration tests using `CrudControllerIntegrationTests`, you'll also need to create a custom `@ControllerAdvice` that extends Allcrud's `AbstractGlobalExceptionHandler`.

This is important because integration tests verify the **HTTP status codes** returned by the controller. These status codes are mapped in `AbstractGlobalExceptionHandler`, so extending it ensures the same error handling behavior during tests as in production.

If you skip this, your exceptions may fall back to Spring Boot's default error handling, which could cause test assertions (like expecting HTTP 422 for a `BusinessException`) to fail.

### 🔍 Example: Testing `Product`

### Service Tests:
```java
@ExtendWith(MockitoExtension.class)
public class ProductServiceTests extends CrudServiceTests<Product, Long> {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    public ProductServiceTests() {
        super(Product.class, Long.class);
    }

    @Override
    protected EntityRepository<Product, Long> getRepository() {
        return repository;
    }

    @Override
    protected CrudService<Product, Long> getService() {
        return service;
    }

    // Your custom tests here
    // Note: All CRUD tests are already implemented by CrudServiceTests
}
```

### Integration Tests:
```java
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerIntegrationTests extends CrudControllerIntegrationTests<Product, ProductVO, Long> {

    @Autowired
    private ProductRepository repository;

    public ProductControllerIntegrationTests() {
        super(ProductVO.class, Long.class);
    }

    @Override
    protected EntityRepository<Product, Long> getRepository() {
        return repository;
    }
    
    
    // Your custom tests here
    // Note: All CRUD tests are already implemented by CrudControllerIntegrationTests
    // 15+ CRUD tests against real PostgreSQL database
}
```

> 💡 If you work with @RequestMapping at class level and want automatic path resolution, pass the controller class as the last parameter. Otherwise, set `basePath` manually.

---
## 📅 Roadmap

Follow evolution and planned versions below:

- [x] ~~MVP with base structure and test support~~
- [x] ~~Refinements from early feedback~~
- [ ] Public release to Maven Central
- [ ] Real-world example application
- [ ] External documentation site
- [ ] Refinements based on community feedback
- [ ] Official stable release

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