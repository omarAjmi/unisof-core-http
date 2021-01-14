package com.unisoft.core.http;

import com.unisoft.core.http.impl.HttpClientProviders;
import reactor.core.publisher.Mono;

/**
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
public interface HttpClient {

    /**
     * Create default {@link HttpClient} instance.
     *
     * @return
     */
    static HttpClient createDefault() {
        return HttpClientProviders.createInstance();
    }

    /**
     * A generic interface for sending HTTP requests and getting responses.
     *
     * @param request
     * @return
     */
    Mono<HttpResponse> send(HttpRequest request);
}
