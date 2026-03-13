package com.example.platform.testsupport;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Centralized Oracle container wiring for integration tests.
 * Uses gvenzl/oracle-free (same as docker-compose) to minimize environment drift.
 */
@Testcontainers
public abstract class OracleTestContainerConfig {

    private static final String APP_USER = "app";
    private static final String APP_PASSWORD = "app";

    @Container
    protected static final GenericContainer<?> ORACLE =
            new GenericContainer<>(DockerImageName.parse("gvenzl/oracle-free:23-slim"))
                    .withEnv("ORACLE_PASSWORD", APP_PASSWORD)
                    .withEnv("APP_USER", APP_USER)
                    .withEnv("APP_USER_PASSWORD", APP_PASSWORD)
                    .withExposedPorts(1521);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        String host = ORACLE.getHost();
        Integer port = ORACLE.getMappedPort(1521);
        String jdbcUrl = "jdbc:oracle:thin:@//" + host + ":" + port + "/FREEPDB1";

        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", () -> APP_USER);
        registry.add("spring.datasource.password", () -> APP_PASSWORD);
        registry.add("spring.datasource.driver-class-name", () -> "oracle.jdbc.OracleDriver");

        // Ensure Flyway runs during tests.
        registry.add("spring.flyway.enabled", () -> "true");

        // Deterministic JWT for tests.
        registry.add("app.jwt.secret", () -> "test-secret-test-secret-test-secret-test-secret");
        registry.add("app.jwt.access-expiration-seconds", () -> "900");
        registry.add("app.jwt.refresh-expiration-seconds", () -> "3600");
    }
}

