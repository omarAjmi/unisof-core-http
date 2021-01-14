package com.unisoft.core.http;

import reactor.core.publisher.Mono;

/**
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
public class NoOpHttpClient implements HttpClient {
    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return Mono.empty(); // NOP
    }

}
