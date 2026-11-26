package com.dio.gof.orders.api;

import com.dio.gof.orders.domain.ShippingMethod;
import com.dio.gof.payment.domain.PaymentMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Checkout & Orders REST API Integration Tests")
class CheckoutControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/checkout should create PAID order with PIX discount and Express shipping, then fetch it via Orders API")
    void shouldProcessCheckoutAndRetrievePersistedOrder() throws Exception {
        CheckoutRequest request = new CheckoutRequest(
                "Lucia Ferreira",
                "lucia@example.com",
                "01001-000",
                ShippingMethod.EXPRESS,
                PaymentMethod.PIX,
                List.of(
                        new OrderItemRequest("Mechanical Keyboard", 1, new BigDecimal("100.00")),
                        new OrderItemRequest("Mouse Pad", 2, new BigDecimal("25.00"))
                )
        );

        MvcResult result = mockMvc.perform(post("/api/v1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/orders/")))
                .andExpect(jsonPath("$.orderId").isNumber())
                .andExpect(jsonPath("$.subtotal").value(150.00))
                .andExpect(jsonPath("$.shippingCost").value(35.00))
                .andExpect(jsonPath("$.estimatedDeliveryDays").value(1))
                .andExpect(jsonPath("$.discountOrFee").value(-18.50))
                .andExpect(jsonPath("$.totalAmount").value(166.50))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paymentTransactionId", containsString("PIX-")))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        long orderId = json.get("orderId").asLong();

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.customerName").value("Lucia Ferreira"))
                .andExpect(jsonPath("$.items", hasSize(2)));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("POST /api/v1/checkout should grant free Economy shipping and apply Credit Card 2.5% fee")
    void shouldProcessCreditCardCheckoutWithFreeEconomyShipping() throws Exception {
        CheckoutRequest request = new CheckoutRequest(
                "Rafael Costa",
                "rafael@example.com",
                "30130-010",
                ShippingMethod.ECONOMY,
                PaymentMethod.CREDIT_CARD,
                List.of(new OrderItemRequest("4K Monitor", 1, new BigDecimal("200.00")))
        );

        mockMvc.perform(post("/api/v1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingCost").value(0.00))
                .andExpect(jsonPath("$.discountOrFee").value(5.00))
                .andExpect(jsonPath("$.totalAmount").value(205.00))
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    @DisplayName("POST /api/v1/checkout should return 400 RFC 7807 ProblemDetail for invalid request payload")
    void shouldReturnBadRequestForInvalidCheckoutPayload() throws Exception {
        CheckoutRequest invalidRequest = new CheckoutRequest(
                "",
                "not-an-email",
                "",
                null,
                null,
                List.of()
        );

        mockMvc.perform(post("/api/v1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados de Entrada Inválidos"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/v1/checkout should return 400 RFC 7807 ProblemDetail for unknown enum value")
    void shouldReturnBadRequestForInvalidEnumValue() throws Exception {
        String invalidEnumJson = """
                {
                  "customerName": "Rafael Costa",
                  "customerEmail": "rafael@example.com",
                  "postalCode": "30130-010",
                  "shippingMethod": "ECONOMY",
                  "paymentMethod": "CRYPTO",
                  "items": [
                    { "productName": "Mouse", "quantity": 1, "unitPrice": 50.00 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEnumJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requisição Malformada"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/v1/checkout should return 400 RFC 7807 ProblemDetail for invalid nested OrderItemRequest")
    void shouldReturnBadRequestForInvalidNestedItem() throws Exception {
        CheckoutRequest invalidItemRequest = new CheckoutRequest(
                "Rafael Costa",
                "rafael@example.com",
                "30130-010",
                ShippingMethod.ECONOMY,
                PaymentMethod.PIX,
                List.of(new OrderItemRequest("", 0, BigDecimal.ZERO))
        );

        mockMvc.perform(post("/api/v1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItemRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados de Entrada Inválidos"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} should return 404 RFC 7807 ProblemDetail when order does not exist")
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/orders/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso Não Encontrado"))
                .andExpect(jsonPath("$.status").value(404));
    }
}
