package com.unisoft.core.http.policy;

import com.unisoft.core.http.*;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HostPolicyTest {
    private static HttpPipeline createPipeline(String host, String expectedUrl) {
        return new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient())
                .policies(new HostPolicy(host),
                        (context, next) -> {
                            assertEquals(expectedUrl, context.getHttpRequest().getUrl().toString());
                            return next.process();
                        })
                .build();
    }

    private static HttpRequest createHttpRequest(String url) throws MalformedURLException {
        return new HttpRequest(HttpMethod.GET, new URL(url));
    }

    @Test
    void withNoPort() throws MalformedURLException {
        final HttpPipeline pipeline = createPipeline("localhost", "ftp://localhost");
        pipeline.send(createHttpRequest("ftp://www.example.com")).block();
    }

    @Test
    void withPort() throws MalformedURLException {
        final HttpPipeline pipeline = createPipeline("localhost", "ftp://localhost:1234");
        pipeline.send(createHttpRequest("ftp://www.example.com:1234"));
    }
}