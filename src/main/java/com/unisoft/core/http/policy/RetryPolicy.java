package com.unisoft.core.http.policy;

import com.unisoft.core.http.HttpPipelineCallContext;
import com.unisoft.core.http.HttpPipelineNextPolicy;
import com.unisoft.core.http.HttpRequest;
import com.unisoft.core.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static com.unisoft.core.util.CoreUtil.isNullOrEmpty;

/**
 * A pipeline policy that retries when a recoverable HTTP error occurs.
 *
 * @author omar.H.Ajmi
 * @since 17/01/2021
 */
public class RetryPolicy implements HttpPipelinePolicy {
    private static final Logger log = LoggerFactory.getLogger(RetryPolicy.class);

    private final RetryStrategy retryStrategy;
    private final String retryAfterHeader;
    private final ChronoUnit retryAfterTimeUnit;

    /**
     * Creates {@link RetryPolicy} with default {@link ExponentialBackoff} as {@link RetryStrategy} and ignore the delay
     * provided in response header.
     */
    public RetryPolicy() {
        this(new ExponentialBackoff(), null, null);
    }

    /**
     * Creates {@link RetryPolicy} with default {@link ExponentialBackoff} as {@link RetryStrategy} and use provided
     * {@code retryAfterHeader} in {@link HttpResponse} headers for calculating retry delay.
     *
     * @param retryAfterHeader   The HTTP header, such as 'Retry-After' or 'x-ms-retry-after-ms', to lookup for the retry
     *                           delay. If the value is null, {@link RetryPolicy} will use the retry strategy to compute the delay and ignore the
     *                           delay provided in response header.
     * @param retryAfterTimeUnit The time unit to use when applying the retry delay. null is valid if, and only if,
     *                           {@code retryAfterHeader} is null.
     * @throws NullPointerException When {@code retryAfterTimeUnit} is null and {@code retryAfterHeader} is not null.
     */
    public RetryPolicy(String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        this(new ExponentialBackoff(), retryAfterHeader, retryAfterTimeUnit);
    }

    /**
     * Creates {@link RetryPolicy} with the provided {@link RetryStrategy} and default {@link ExponentialBackoff} as
     * {@link RetryStrategy}. It will use provided {@code retryAfterHeader} in {@link HttpResponse} headers for
     * calculating retry delay.
     *
     * @param retryStrategy      The {@link RetryStrategy} used for retries.
     * @param retryAfterHeader   The HTTP header, such as 'Retry-After' or 'x-ms-retry-after-ms', to lookup for the retry
     *                           delay. If the value is null, {@link RetryPolicy} will use the retry strategy to compute the delay and ignore the
     *                           delay provided in response header.
     * @param retryAfterTimeUnit The time unit to use when applying the retry delay. null is valid if, and only if,
     *                           {@code retryAfterHeader} is null.
     * @throws NullPointerException When {@code retryStrategy} is null. Also when {@code retryAfterTimeUnit} is null and
     *                              {@code retryAfterHeader} is not null.
     */
    public RetryPolicy(RetryStrategy retryStrategy, String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        this.retryStrategy = Objects.requireNonNull(retryStrategy, "'retryStrategy' cannot be null.");
        this.retryAfterHeader = retryAfterHeader;
        this.retryAfterTimeUnit = retryAfterTimeUnit;
        if (!isNullOrEmpty(retryAfterHeader)) {
            Objects.requireNonNull(retryAfterTimeUnit, "'retryAfterTimeUnit' cannot be null.");
        }
    }

    /**
     * Creates a {@link RetryPolicy} with the provided {@link RetryStrategy} and ignore the delay provided in response
     * header.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     * @throws NullPointerException When {@code retryStrategy} is null.
     */
    public RetryPolicy(RetryStrategy retryStrategy) {
        this(retryStrategy, null, null);
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return attemptAsync(context, next, context.getHttpRequest(), 0);
    }

    private Mono<HttpResponse> attemptAsync(final HttpPipelineCallContext context, final HttpPipelineNextPolicy next,
                                            final HttpRequest originalHttpRequest, final int tryCount) {
        context.setHttpRequest(originalHttpRequest.copy());
        return next.clone().process()
                .flatMap(httpResponse -> {
                    if (shouldRetry(httpResponse, tryCount)) {
                        final Duration delayDuration = determineDelayDuration(httpResponse, tryCount);
                        log.info("[Retrying] Try count: {}, Delay duration in seconds: {}", tryCount,
                                delayDuration.getSeconds());

                        Flux<ByteBuffer> responseBody = httpResponse.getBody();
                        if (responseBody == null) {
                            return attemptAsync(context, next, originalHttpRequest, tryCount + 1)
                                    .delaySubscription(delayDuration);
                        } else {
                            return httpResponse.getBody()
                                    .ignoreElements()
                                    .then(attemptAsync(context, next, originalHttpRequest, tryCount + 1)
                                            .delaySubscription(delayDuration));
                        }
                    } else {
                        return Mono.just(httpResponse);
                    }
                })
                .onErrorResume(err -> {
                    int maxRetries = retryStrategy.getMaxRetries();
                    if (tryCount < maxRetries) {
                        log.info("[Error Resume] Try count: {}, Error: {}", tryCount, err);
                        return attemptAsync(context, next, originalHttpRequest, tryCount + 1)
                                .delaySubscription(retryStrategy.calculateRetryDelay(tryCount));
                    } else {
                        return Mono.error(new RuntimeException(
                                String.format("Max retries %d times exceeded. Error Details: %s", maxRetries, err.getMessage()),
                                err));
                    }
                });
    }

    private boolean shouldRetry(HttpResponse response, int tryCount) {
        return tryCount < retryStrategy.getMaxRetries()
                && retryStrategy.shouldRetry(response);
    }

    /**
     * Determines the delay duration that should be waited before retrying.
     *
     * @param response HTTP response
     * @return If the HTTP response has a retry-after-ms header that will be returned, otherwise the duration used
     * during the construction of the policy.
     */
    private Duration determineDelayDuration(HttpResponse response, int tryCount) {
        int code = response.getStatusCode();

        // Response will not have a retry-after-ms header.
        if (code != 429        // too many requests
                && code != 503) {  // service unavailable
            return retryStrategy.calculateRetryDelay(tryCount);
        }

        String retryHeaderValue = null;

        if (!isNullOrEmpty(this.retryAfterHeader)) {
            retryHeaderValue = response.getHeaderValue(this.retryAfterHeader);
        }

        // Retry header is missing or empty, return the default delay duration.
        if (isNullOrEmpty(retryHeaderValue)) {
            return this.retryStrategy.calculateRetryDelay(tryCount);
        }

        // Use the response delay duration, the server returned it for a reason.
        return Duration.of(Integer.parseInt(retryHeaderValue), this.retryAfterTimeUnit);
    }
}
