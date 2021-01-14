package com.unisoft.core.http.policy;

import com.unisoft.core.http.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class TimeoutPolicyTest {
    @Test
    void createPolicyWithTimeoutDoesNotExceed() {
        HttpPipeline httpPipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        return Mono.just(((HttpResponse) new MockHttpResponse(request, 200))).delaySubscription(Duration.ofSeconds(1));
                    }
                })
                .policies(new TimeoutPolicy(Duration.ofSeconds(2)))
                .build();

        assertDoesNotThrow(() -> httpPipeline.send(new HttpRequest(HttpMethod.GET, "http://www.test.com")).block());
    }

    @Test
    void createPolicyWithTimeoutDoesExceed() {
        HttpPipeline httpPipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        return Mono.just(((HttpResponse) new MockHttpResponse(request, 200))).delaySubscription(Duration.ofSeconds(2));
                    }
                })
                .policies(new TimeoutPolicy(Duration.ofSeconds(1)))
                .build();
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://www.test.com");
        HttpResponse httpResponse = httpPipeline.send(httpRequest)
                .onErrorResume(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        return Mono.just(new MockHttpResponse(httpRequest, 408));
                    } else {
                        return Mono.just(new MockHttpResponse(httpRequest, 500));
                    }
                })
                .block();
        assertEquals(408, httpResponse.getStatusCode());
    }

    @Test
    void createWithTimeoutNullThrows() {
        assertThrows(NullPointerException.class, () -> new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        return Mono.just(((HttpResponse) new MockHttpResponse(request, 200))).delaySubscription(Duration.ofSeconds(1));
                    }
                })
                .policies(new TimeoutPolicy(null))
                .build());
    }
}