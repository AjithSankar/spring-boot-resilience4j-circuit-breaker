package dev.ak.springbootresilience4j.ratelimiter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@Slf4j
public class RateLimiterExample {

    public static void main(String[] args) {

        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                .limitForPeriod(5) // Number of permissions/calls allowed in a period
                .limitRefreshPeriod(Duration.ofSeconds(1)) // Period after which the permissions/calls are reset
                .timeoutDuration(Duration.ofMillis(500)) // Max wait time 500ms if rate limit is reached
                .build();

        RateLimiter rateLimiter = RateLimiter.of("myRateLimiter", rateLimiterConfig);

        Supplier<String> limitedCall = RateLimiter.decorateSupplier(rateLimiter, RateLimiterExample:: callExternalAPI);

        /*IntStream.range(1, 51).forEach( i  -> {
            try {
                System.out.println("Request " +  i + ": " + limitedCall.get());
            } catch (RequestNotPermitted e) {
                System.out.println("Request " + i + ": Rate limit exceeded, Try again ...");
            }
        });*/

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        IntStream.range(1, 51).forEach( i  -> {

            executorService.submit(() -> {
                try {
                    System.out.println("Request " +  i + ": " + limitedCall.get());
                } catch (RequestNotPermitted e) {
                    System.out.println("Request " + i + ": Rate limit exceeded, Try again ...");
                }
            });

        });

        executorService.shutdown();

        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static String callExternalAPI() {
        return "API Response at " + System.currentTimeMillis();
    }

}
