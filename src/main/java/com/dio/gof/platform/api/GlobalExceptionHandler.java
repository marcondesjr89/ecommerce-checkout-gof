package com.dio.gof.platform.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String ERROR_BASE_URI = "https://ecommerce-checkout-gof/errors/";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildProblemDetail(
                HttpStatus.NOT_FOUND,
                "Recurso Não Encontrado",
                ex.getMessage(),
                "resource-not-found"
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException ex) {
        return buildProblemDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Regra de Negócio Violada",
                ex.getMessage(),
                "business-rule"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Dados de Entrada Inválidos",
                detail,
                "validation-error"
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Argumento Inválido",
                ex.getMessage(),
                "invalid-argument"
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception ex) {
        log.error("Unhandled internal server error", ex);
        return buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro Interno",
                "Ocorreu um erro interno inesperado. Tente novamente mais tarde.",
                "internal-error"
        );
    }

    private ProblemDetail buildProblemDetail(HttpStatus status, String title, String detail, String errorSlug) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(ERROR_BASE_URI + errorSlug));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
