package com.unisoft.core.http.policy;

import com.unisoft.core.http.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddHeadersPolicyTest {
    @Test
    void createWithHeadersExist() {
        HttpHeaders headers = new HttpHeaders();
        headers.put("h1", "v1")
                .put("h2", "v2");
        HttpPipeline httpPipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        assertEquals(2, request.getHeaders().getSize());
                        assertEquals("v1", request.getHeaders().getValue("h1"));
                        assertEquals("v2", request.getHeaders().getValue("h2"));
                        return Mono.empty();
                    }
                })
                .policies(new AddHeadersPolicy(headers))
                .build();

        httpPipeline.send(new HttpRequest(HttpMethod.GET, "http://www.test.com")).block();
    }
}