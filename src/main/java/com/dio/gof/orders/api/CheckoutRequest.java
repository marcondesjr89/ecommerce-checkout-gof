package com.dio.gof.orders.api;

import com.dio.gof.orders.domain.ShippingMethod;
import com.dio.gof.payment.domain.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CheckoutRequest(
        @NotBlank(message = "O nome do cliente é obrigatório")
        String customerName,

        @NotBlank(message = "O e-mail do cliente é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String customerEmail,

        @NotBlank(message = "O CEP de entrega é obrigatório")
        String postalCode,

        @NotNull(message = "O método de frete é obrigatório (EXPRESS ou ECONOMY)")
        ShippingMethod shippingMethod,

        @NotNull(message = "A forma de pagamento é obrigatória (PIX, CREDIT_CARD ou BOLETO)")
        PaymentMethod paymentMethod,

        @NotEmpty(message = "O pedido deve conter pelo menos um item")
        @Valid
        List<OrderItemRequest> items
) {
}
