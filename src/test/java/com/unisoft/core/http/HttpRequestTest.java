package com.unisoft.core.http;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestTest {

    @Test
    void constructor() throws MalformedURLException {
        final HttpRequest request = new HttpRequest(HttpMethod.POST, new URL("http://request.url"));
        assertEquals(HttpMethod.POST, request.getHttpMethod());
        assertEquals(new URL("http://request.url"), request.getUrl());
    }

    @Test
    void testClone() throws IOException {
        final HttpHeaders headers = new HttpHeaders().put("my-header", "my-value")
                .put("other-header", "other-value");

        final HttpRequest request = new HttpRequest(
                HttpMethod.PUT,
                new URL("http://request.url"),
                headers,
                Flux.empty());

        final HttpRequest bufferedRequest = request.copy();

        assertNotSame(request, bufferedRequest);

        assertEquals(request.getHttpMethod(), bufferedRequest.getHttpMethod());
        assertEquals(request.getUrl(), bufferedRequest.getUrl());

        assertNotSame(request.getHeaders(), bufferedRequest.getHeaders());
        assertEquals(request.getHeaders().getSize(), bufferedRequest.getHeaders().getSize());
        for (HttpHeader clonedHeader : bufferedRequest.getHeaders()) {
            for (HttpHeader originalHeader : request.getHeaders()) {
                assertNotSame(clonedHeader, originalHeader);
            }

            assertEquals(clonedHeader.getValue(), request.getHeaders().getValue(clonedHeader.getName()));
        }

        assertSame(request.getBody(), bufferedRequest.getBody());
    }
}