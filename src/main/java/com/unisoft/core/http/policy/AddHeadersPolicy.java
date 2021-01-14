package com.unisoft.core.http.policy;

import com.unisoft.core.http.*;
import reactor.core.publisher.Mono;

/**
 * The pipeline policy that adds a particular set of headers to HTTP requests.
 *
 * @author omar.H.Ajmi
 * @since 19/10/2020
 */
public class AddHeadersPolicy implements HttpPipelinePolicy {
    private final HttpHeaders headers;

    /**
     * Creates a AddHeadersPolicy.
     *
     * @param headers The headers to add to outgoing requests.
     */
    public AddHeadersPolicy(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        for (HttpHeader header : headers) {
            context.getHttpRequest().setHeader(header.getName(), header.getValue());
        }
        return next.process();
    }
}
