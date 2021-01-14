package com.unisoft.core.http;

import com.unisoft.core.http.policy.HostPolicy;
import com.unisoft.core.http.policy.PortPolicy;
import com.unisoft.core.http.policy.ProtocolPolicy;
import com.unisoft.core.http.util.UrlBuilder;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class HttpPipelineTest {
    @Test
    void constructorWithNoArguments() {
        HttpPipeline pipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        // do nothing
                        return null;
                    }
                }).build();
        assertEquals(0, pipeline.getPolicyCount());
        assertNotNull(pipeline.getHttpClient());
    }

    @Test
    void withRequestPolicy() {
        HttpPipeline pipeline = new HttpPipelineBuilder()
                .policies(new PortPolicy(80, true),
                        new ProtocolPolicy("ftp", true))
                .httpClient(new NoOpHttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        // do nothing
                        return null;
                    }
                }).build();

        assertEquals(2, pipeline.getPolicyCount());
        assertEquals(PortPolicy.class, pipeline.getPolicy(0).getClass());
        assertEquals(ProtocolPolicy.class, pipeline.getPolicy(1).getClass());
        assertNotNull(pipeline.getHttpClient());
    }

    @Test
    void withRequestOptions() throws MalformedURLException {
        HttpPipeline pipeline = new HttpPipelineBuilder()
                .policies(new PortPolicy(80, true),
                        new ProtocolPolicy("ftp", true))
                .httpClient(new NoOpHttpClient()).build();

        HttpPipelineCallContext context = new HttpPipelineCallContext(new HttpRequest(HttpMethod.GET, new URL("http://foo.com")));
        assertNotNull(context);
        assertNotNull(pipeline.getHttpClient());
    }

    @Test
    void withNoRequestPolicies() throws MalformedURLException {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final URL expectedUrl = new URL("http://my.site.com");
        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        assertEquals(0, request.getHeaders().getSize());
                        assertEquals(expectedHttpMethod, request.getHttpMethod());
                        assertEquals(expectedUrl, request.getUrl());
                        return Mono.just(new MockHttpResponse(request, 200));
                    }
                })
                .build();

        final HttpResponse response = httpPipeline.send(new HttpRequest(expectedHttpMethod, expectedUrl)).block();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void withPortPolicy() throws MalformedURLException {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final URL expectedUrl = new URL("http://my.site.com:8000/1");
        final int expectedPort = UrlBuilder.parse(expectedUrl).getPort();
        final HttpClient httpClient = new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertNotNull(request.getUrl());
                assertEquals(expectedPort, request.getUrl().getPort());
                assertEquals(expectedHttpMethod, request.getHttpMethod());
                assertEquals(expectedUrl, request.getUrl());
                return Mono.just(new MockHttpResponse(request, 200));
            }
        };

        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
                .httpClient(httpClient)
                .policies(new PortPolicy(expectedPort, true))
                .build();

        final HttpResponse response = httpPipeline.send(new HttpRequest(expectedHttpMethod, expectedUrl)).block();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void withHostPolicy() throws MalformedURLException {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final URL expectedUrl = new URL("http://my.site.com/1");
        final String expectedHost = UrlBuilder.parse(expectedUrl).getHost();
        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient() {
                    @Override
                    public Mono<HttpResponse> send(HttpRequest request) {
                        final String host = request.getUrl().getHost();
                        assertNotNull(host);
                        assertFalse(host.isEmpty());

                        assertEquals(expectedHttpMethod, request.getHttpMethod());
                        assertEquals(expectedUrl, request.getUrl());
                        assertEquals(expectedHost, request.getUrl().getHost());
                        return Mono.just(new MockHttpResponse(request, 200));
                    }
                })
                .policies(new HostPolicy(expectedHost))
                .build();

        final HttpResponse response = httpPipeline.send(new HttpRequest(expectedHttpMethod, expectedUrl)).block();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }
}
