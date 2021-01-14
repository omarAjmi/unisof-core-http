package com.unisoft.core.http.policy;

import com.unisoft.core.http.*;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProtocolPolicyTest {
    private static HttpPipeline createPipeline(String protocol, String expectedUrl) {
        return new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient())
                .policies(new ProtocolPolicy(protocol, true),
                        (context, next) -> {
                            assertEquals(expectedUrl, context.getHttpRequest().getUrl().toString());
                            return next.process();
                        })
                .build();
    }

    private static HttpPipeline createPipeline(String protocol, boolean overwrite, String expectedUrl) {
        return new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient())
                .policies(new ProtocolPolicy(protocol, overwrite),
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
    void withOverwrite() throws MalformedURLException {
        final HttpPipeline pipeline = createPipeline("ftp", "ftp://www.bing.com");
        pipeline.send(createHttpRequest("http://www.bing.com"));
    }

    @Test
    void withNoOverwrite() throws MalformedURLException {
        final HttpPipeline pipeline = createPipeline("ftp", false, "https://www.bing.com");
        pipeline.send(createHttpRequest("https://www.bing.com"));
    }
}