package com.dio.gof.platform.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * GoF Pattern: Singleton (Creational)
 *
 * In Spring Framework, beans annotated with @Component, @Service, or @Configuration
 * default to SINGLETON scope within the ApplicationContext.
 *
 * This guarantees a single shared, thread-safe instance across the application lifecycle
 * without manual double-checked locking boilerplate. Mutable state (event counter) uses
 * lock-free AtomicLong to remain strictly thread-safe under concurrent requests.
 */
@Component
public class AuditLogger {

    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);

    private final String instanceId;
    private final AtomicLong totalLoggedEvents;

    public AuditLogger() {
        this.instanceId = UUID.randomUUID().toString();
        this.totalLoggedEvents = new AtomicLong(0L);
        log.info("[SINGLETON] AuditLogger initialized. instanceId={}", this.instanceId);
    }

    public String getInstanceId() {
        return instanceId;
    }

    public long getTotalLoggedEvents() {
        return totalLoggedEvents.get();
    }

    public void logCheckoutEvent(Long orderId, String customerEmail, String paymentMethod, String shippingMethod) {
        long eventSequence = totalLoggedEvents.incrementAndGet();
        log.info(
                "[AUDIT] event={} instanceId={} orderId={} customerEmail={} paymentMethod={} shippingMethod={} timestamp={}",
                eventSequence,
                instanceId,
                orderId,
                customerEmail,
                paymentMethod,
                shippingMethod,
                Instant.now()
        );
    }
}
