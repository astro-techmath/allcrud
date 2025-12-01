package com.techmath.allcrud.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Singleton container configuration for integration tests.
 * <p>
 * Provides a single shared PostgreSQL container instance that is reused
 * across all test classes in the JVM to improve test execution performance.
 * <p>
 * The container is automatically started on first access and stopped when the JVM exits.
 *
 * @author Matheus Maia
 */
public final class TestContainerConfig {

    private TestContainerConfig() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * PostgreSQL Docker image to use for testing.
     * Using alpine variant for smaller image size and faster startup.
     */
    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:16-alpine");

    /**
     * Singleton container instance.
     * Volatile ensures visibility across threads.
     */
    private static volatile PostgreSQLContainer<?> container;

    /**
     * Returns the shared PostgreSQL container instance.
     * <p>
     * The container is lazily initialized and started on first access.
     * Uses double-checked locking for thread-safe singleton initialization.
     *
     * @return the PostgreSQL container instance
     */
    public static PostgreSQLContainer<?> getPostgresContainer() {
        if (container == null) {
            synchronized (TestContainerConfig.class) {
                if (container == null) {
                    container = createContainer();
                    container.start();
                }
            }
        }
        return container;
    }

    /**
     * Creates and configures a new PostgreSQL container instance.
     *
     * @return configured PostgreSQL container
     */
    private static PostgreSQLContainer<?> createContainer() {
        return new PostgreSQLContainer<>(POSTGRES_IMAGE)
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(false);  // Explicitly false for CI/CD compatibility
    }

    /**
     * Configures Spring DataSource properties to use the Testcontainer.
     * <p>
     * This method should be called from a {@code @DynamicPropertySource} method
     * in your test class.
     *
     * @param registry the dynamic property registry
     */
    public static void configureDataSource(DynamicPropertyRegistry registry) {
        PostgreSQLContainer<?> postgres = getPostgresContainer();

        // DataSource properties
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);

        // JPA/Hibernate configuration
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "false");
    }

}
