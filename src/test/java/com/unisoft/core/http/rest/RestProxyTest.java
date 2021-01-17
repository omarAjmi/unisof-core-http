package com.unisoft.core.http.rest;

import com.unisoft.core.http.HttpMethod;
import com.unisoft.core.http.HttpRequest;
import com.unisoft.core.http.exception.UnexpectedLengthException;
import com.unisoft.core.util.FluxUtil;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class RestProxyTest {
    private static final byte[] EXPECTED = new byte[]{0, 1, 2, 3, 4};

    private static Mono<byte[]> collectRequest(HttpRequest request) {
        return FluxUtil.collectBytesInByteBufferStream(RestProxy.validateLength(request));
    }

    @Test
    public void emptyRequestBody() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost");

        StepVerifier.create(RestProxy.validateLength(httpRequest))
                .verifyComplete();
    }

    @Test
    public void expectedBodyLength() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
                .setBody(EXPECTED)
                .setHeader("Content-Length", "5");

        StepVerifier.create(collectRequest(httpRequest))
                .assertNext(bytes -> assertArrayEquals(EXPECTED, bytes))
                .verifyComplete();
    }

    @Test
    public void unexpectedBodyLength() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
                .setBody(EXPECTED);

        StepVerifier.create(collectRequest(httpRequest.setHeader("Content-Length", "4")))
                .verifyErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof UnexpectedLengthException);
                    assertEquals("Request body emitted 5 bytes, more than the expected 4 bytes.", throwable.getMessage());
                });

        StepVerifier.create(collectRequest(httpRequest.setHeader("Content-Length", "6")))
                .verifyErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof UnexpectedLengthException);
                    assertEquals("Request body emitted 5 bytes, less than the expected 6 bytes.", throwable.getMessage());
                });
    }

    @Test
    public void multipleSubscriptionsToCheckBodyLength() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost")
                .setBody(EXPECTED)
                .setHeader("Content-Length", "5");

        Flux<ByteBuffer> verifierFlux = RestProxy.validateLength(httpRequest);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(verifierFlux))
                .assertNext(bytes -> assertArrayEquals(EXPECTED, bytes))
                .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(verifierFlux))
                .assertNext(bytes -> assertArrayEquals(EXPECTED, bytes))
                .verifyComplete();
    }
}