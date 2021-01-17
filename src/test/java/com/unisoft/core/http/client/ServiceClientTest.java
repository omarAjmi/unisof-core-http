package com.unisoft.core.http.client;

import com.unisoft.core.http.HttpPipelineBuilder;
import com.unisoft.core.http.NoOpHttpClient;
import com.unisoft.core.http.serialize.JacksonAdapter;
import com.unisoft.core.http.testable.TestServiceClientBuilder;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceClientTest {

    private final String expectedUrl = "www.foo.bar";
    private final int expectedRetry = 3;
    private final Duration expectedTimeout = Duration.ofSeconds(3);

    @Test
    void build() {
        ServiceClientOptions clientOptions = assertDoesNotThrow(this::buildClientOptions);
        assertEquals(expectedUrl, clientOptions.getEndpoint());
        assertEquals(expectedRetry, clientOptions.getMaxRetry());
        assertEquals(expectedTimeout, clientOptions.getTimeout());

        ServiceClient.Builder builder = new TestServiceClientBuilder()
                .options(clientOptions)
                .httpPipeline(new HttpPipelineBuilder().httpClient(new NoOpHttpClient()).build())
                .serializerAdapter(JacksonAdapter.createDefaultSerializerAdapter());
        ServiceClient serviceClient = assertDoesNotThrow(builder::build);

        assertEquals(clientOptions, serviceClient.getOptions());
    }

    private ServiceClientOptions buildClientOptions() {
        return new ServiceClientOptions.Builder()
                .baseUrl(expectedUrl)
                .maxRetry(expectedRetry)
                .timeout(expectedTimeout).build();
    }
}