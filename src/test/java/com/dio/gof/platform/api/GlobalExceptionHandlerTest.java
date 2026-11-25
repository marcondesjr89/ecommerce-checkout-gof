package com.dio.gof.platform.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("GlobalExceptionHandler RFC 7807 Tests")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DummyErrorController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 422 ProblemDetail for BusinessException")
    void shouldReturnUnprocessableEntityForBusinessException() throws Exception {
        mockMvc.perform(get("/test-errors/business"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Regra de Negócio Violada"))
                .andExpect(jsonPath("$.detail").value("Método de pagamento não suportado."))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should return 400 ProblemDetail with field details for validation errors")
    void shouldReturnBadRequestForValidationErrors() throws Exception {
        mockMvc.perform(post("/test-errors/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados de Entrada Inválidos"))
                .andExpect(jsonPath("$.detail", containsString("name")))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should return 400 ProblemDetail for IllegalArgumentException")
    void shouldReturnBadRequestForIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/test-errors/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Argumento Inválido"))
                .andExpect(jsonPath("$.detail").value("Valor informado é inválido."))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should return 500 ProblemDetail without leaking internal exception details")
    void shouldReturnInternalServerErrorWithoutLeakingSensitiveDetails() throws Exception {
        mockMvc.perform(get("/test-errors/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Erro Interno"))
                .andExpect(jsonPath("$.detail", not(containsString("jdbc:postgresql"))))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    record DummyPayload(@NotBlank(message = "must not be blank") String name) {
    }

    @RestController
    @RequestMapping("/test-errors")
    static class DummyErrorController {

        @GetMapping("/business")
        void throwBusiness() {
            throw new BusinessException("Método de pagamento não suportado.");
        }

        @PostMapping("/validation")
        void validatePayload(@RequestBody @Valid DummyPayload payload) {
        }

        @GetMapping("/illegal-argument")
        void throwIllegalArgument() {
            throw new IllegalArgumentException("Valor informado é inválido.");
        }

        @GetMapping("/unexpected")
        void throwUnexpected() {
            throw new RuntimeException("Sensitive internal error: jdbc:postgresql://db:5432/secret");
        }
    }
}
