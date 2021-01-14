package com.unisoft.core.http.policy;

import com.unisoft.core.http.HttpPipelineCallContext;
import com.unisoft.core.http.HttpPipelineNextPolicy;
import com.unisoft.core.http.HttpResponse;
import reactor.core.publisher.Mono;

/**
 * A policy within the {@link com.unisoft.core.http.HttpPipeline}.
 *
 * @author omar.H.Ajmi
 * @see com.unisoft.core.http.HttpPipeline
 * @since 18/10/2020
 */
@FunctionalInterface
public interface HttpPipelinePolicy {

    /**
     * Processes provided request context and invokes the next policy.
     *
     * @param context The request context.
     * @param next    The next policy to invoke.
     * @return A publisher that initiates the request upon subscription and emits a response on completion.
     */
    Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next);
}
