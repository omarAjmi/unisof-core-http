package com.unisoft.core.http.policy;

import com.unisoft.core.http.HttpPipelineCallContext;
import com.unisoft.core.http.HttpPipelineNextPolicy;
import com.unisoft.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

/**
 * The pipeline policy that limits the time allowed between sending a request and receiving the response.
 *
 * @author omar.H.Ajmi
 * @since 19/10/2020
 */
public class TimeoutPolicy implements HttpPipelinePolicy {
    private final Duration timoutDuration;

    /**
     * Creates a TimeoutPolicy.
     *
     * @param timoutDuration the timeout duration
     */
    public TimeoutPolicy(Duration timoutDuration) {
        Objects.requireNonNull(timoutDuration, "'timout-duration' cannot be null");
        this.timoutDuration = timoutDuration;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process().timeout(this.timoutDuration);
    }
}
