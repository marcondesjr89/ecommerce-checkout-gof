package com.dio.gof.orders.api;

import com.dio.gof.orders.application.CheckoutFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/checkout")
@Tag(name = "Checkout", description = "Unified checkout endpoint backed by GoF Facade, Strategy, and Singleton patterns")
public class CheckoutController {

    private final CheckoutFacade checkoutFacade;

    public CheckoutController(CheckoutFacade checkoutFacade) {
        this.checkoutFacade = checkoutFacade;
    }

    @PostMapping
    @Operation(
            summary = "Process complete checkout",
            description = "Invokes CheckoutFacade (GoF Facade) to orchestrate shipping (Strategy), payment (Strategy), persistence, and auditing (Singleton)."
    )
    public ResponseEntity<CheckoutResponse> processCheckout(@RequestBody @Valid CheckoutRequest request) {
        CheckoutResponse response = checkoutFacade.processCheckout(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/orders/{id}")
                .buildAndExpand(response.orderId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }
}
