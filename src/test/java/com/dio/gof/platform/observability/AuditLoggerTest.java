package com.dio.gof.platform.observability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("GoF Singleton Pattern - AuditLogger Tests")
class AuditLoggerTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AuditLogger firstInjectedLogger;

    @Autowired
    private AuditLogger secondInjectedLogger;

    @Test
    @DisplayName("Should guarantee a single shared instance managed by Spring IoC Container")
    void shouldGuaranteeSingleInstanceInApplicationContext() {
        AuditLogger lookedUpLogger = applicationContext.getBean(AuditLogger.class);

        assertThat(firstInjectedLogger).isSameAs(secondInjectedLogger);
        assertThat(firstInjectedLogger).isSameAs(lookedUpLogger);
        assertThat(firstInjectedLogger.getInstanceId())
                .isNotBlank()
                .isEqualTo(lookedUpLogger.getInstanceId());
    }

    @Test
    @DisplayName("Should track checkout audit events in a thread-safe manner")
    void shouldTrackEventsSafelyAcrossConcurrentThreads() throws InterruptedException {
        AuditLogger isolatedLogger = new AuditLogger();
        int threadCount = 10;
        int eventsPerThread = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);

        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            for (int i = 0; i < threadCount; i++) {
                final int worker = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < eventsPerThread; j++) {
                            isolatedLogger.logCheckoutEvent(
                                    (long) (worker * eventsPerThread + j),
                                    "customer" + worker + "@example.com",
                                    "PIX",
                                    "EXPRESS"
                            );
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            boolean finished = latch.await(5, TimeUnit.SECONDS);
            assertThat(finished).isTrue();
        }

        assertThat(isolatedLogger.getTotalLoggedEvents()).isEqualTo((long) threadCount * eventsPerThread);
    }
}
