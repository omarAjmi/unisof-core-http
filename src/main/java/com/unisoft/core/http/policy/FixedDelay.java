package com.unisoft.core.http.policy;

import com.unisoft.core.util.log.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;

/**
 * A fixed-delay implementation of {@link RetryStrategy} that has a fixed delay duration between each retry attempt.
 */
public class FixedDelay implements RetryStrategy {

    private static final Logger log = LoggerFactory.getLogger(FixedDelay.class);
    private final int maxRetries;
    private final Duration delay;

    /**
     * Creates an instance of {@link FixedDelay}.
     *
     * @param maxRetries The max number of retry attempts that can be made.
     * @param delay      The fixed delay duration between retry attempts.
     */
    public FixedDelay(int maxRetries, Duration delay) {
        if (maxRetries < 0) {
            throw LogUtil.logExceptionAsError(log, new IllegalArgumentException("Max retries cannot be less than 0."));
        }
        this.maxRetries = maxRetries;
        this.delay = Objects.requireNonNull(delay, "'delay' cannot be null.");
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public Duration calculateRetryDelay(int retryAttempts) {
        return delay;
    }
}
