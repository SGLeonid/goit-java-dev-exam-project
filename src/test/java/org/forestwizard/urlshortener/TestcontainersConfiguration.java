package org.forestwizard.urlshortener;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

import java.util.TimeZone;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        return new PostgreSQLContainer(DockerImageName.parse("postgres:16"));
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
