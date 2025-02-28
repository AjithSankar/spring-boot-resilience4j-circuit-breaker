package dev.ak.springbootresilience4j.retry;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

import java.io.IOException;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class RetryExamples {

    public static void main(String[] args) {

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofMillis(2000))
                .retryExceptions(RuntimeException.class)
                .failAfterMaxAttempts(true)
                .build();
        RetryRegistry registry = RetryRegistry.of(retryConfig);
        Retry retry = registry.retry("apiRetry");

        // Add Event Listener to Log Retries
        retry.getEventPublisher()
                .onRetry(event -> System.out.println("Retrying... Attempt: " + event.getNumberOfRetryAttempts()));

        Supplier<String> retryableSupplier = Retry.decorateSupplier(retry, RetryExamples::unreliableApi);

        try {
            System.out.println("Final Result: " + retryableSupplier.get());
        } catch (Exception e) {
            System.out.println("API failed after retries: " + e.getMessage());
        }


    }

    private static String unreliableApi() {
        if (new Random().nextBoolean()) {
            throw new RuntimeException("API Call Failed!");
        }
        return "API Response at " + System.currentTimeMillis();
    }
}
