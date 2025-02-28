package dev.ak.springbootresilience4j.ratelimiter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

import java.time.Duration;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class RateLimiterAndRetryExample {

    public static void main(String[] args) {

        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                .limitForPeriod(3)
                .limitRefreshPeriod(Duration.ofSeconds(5))
                .timeoutDuration(Duration.ofMillis(1000))
                .build();
        RateLimiter rateLimiter = RateLimiter.of("apiRateLimiter", rateLimiterConfig);

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(2))
                .retryExceptions(RuntimeException.class)
                .build();

        RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);
        Retry retry = retryRegistry.retry("apiRetry");

        retry.getEventPublisher().onRetry(event -> {
            System.out.println("Retry attempt : " + event.getNumberOfRetryAttempts());
        });


        Supplier<String> retryableSupplier = RateLimiter.decorateSupplier(rateLimiter, Retry.decorateSupplier(retry, RateLimiterAndRetryExample::unreliableApi));

        IntStream.range(1, 6).forEach(i  -> {
            try {
                System.out.println("Request " +  i + ": " + retryableSupplier.get());
            } catch (Exception e) {
                System.out.println("Request " + i + ": Rate limit exceeded or API failed after retries. ...");
            }
        });


    }

    private static String unreliableApi() {
        if (new Random().nextBoolean()) {
            throw new RuntimeException("API Call Failed!");
        }
        return "API Response at " + System.currentTimeMillis();
    }
}
