package com.dio.gof.orders.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderItemRequest(
        @NotBlank(message = "O nome do produto é obrigatório")
        String productName,

        @NotNull(message = "A quantidade é obrigatória")
        @Min(value = 1, message = "A quantidade mínima deve ser 1")
        Integer quantity,

        @NotNull(message = "O preço unitário é obrigatório")
        @DecimalMin(value = "0.01", message = "O preço unitário deve ser maior que zero")
        BigDecimal unitPrice
) {
}
