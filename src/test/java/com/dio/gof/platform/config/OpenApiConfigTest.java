package com.dio.gof.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "springdoc.api-docs.enabled=true")
@DisplayName("OpenAPI Configuration & Contract Tests")
class OpenApiConfigTest {

    @Autowired
    private OpenAPI openAPI;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should configure OpenAPI metadata documenting GoF patterns")
    void shouldConfigureOpenApiMetadata() {
        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).contains("GoF");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openAPI.getInfo().getDescription())
                .contains("Strategy")
                .contains("Facade")
                .contains("Singleton");
    }

    @Test
    @DisplayName("Should expose valid OpenAPI 3 specification at /v3/api-docs")
    void shouldExposeValidOpenApiDocsEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").exists())
                .andExpect(jsonPath("$.info.version").value("1.0.0"));
    }
}
