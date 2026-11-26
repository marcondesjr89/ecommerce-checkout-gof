package com.dio.gof.payment.domain;

import java.math.BigDecimal;

/**
 * GoF Pattern: Strategy (Behavioral)
 *
 * Defines a family of interchangeable payment processing algorithms,
 * encapsulating each payment rule independently of the clients that use it.
 */
public interface PaymentStrategy {

    PaymentMethod getMethod();

    PaymentResult process(BigDecimal amount, String customerEmail);
}
