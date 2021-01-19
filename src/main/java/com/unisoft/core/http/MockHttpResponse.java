package com.unisoft.core.http;

import com.unisoft.core.http.serialize.JacksonAdapter;
import com.unisoft.core.http.serialize.SerializerAdapter;
import com.unisoft.core.http.serialize.SerializerEncoding;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * a mock useful mainly for testing
 *
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
public class MockHttpResponse extends HttpResponse {
    private static final SerializerAdapter SERIALIZER = new JacksonAdapter();

    private final int statusCode;

    private final HttpHeaders headers;

    private final byte[] bodyBytes;

    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, byte[] bodyBytes) {
        super(request);
        this.statusCode = statusCode;
        this.headers = headers;
        this.bodyBytes = bodyBytes;
    }

    public MockHttpResponse(HttpRequest request, int statusCode) {
        this(request, statusCode, new HttpHeaders(), new byte[0]);
    }

    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers) {
        this(request, statusCode, headers, new byte[0]);
    }

    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, Object serializable) {
        this(request, statusCode, headers, serialize(serializable));
    }

    public MockHttpResponse(HttpRequest request, int statusCode, Object serializable) {
        this(request, statusCode, new HttpHeaders(), serialize(serializable));
    }

    private static byte[] serialize(Object serializable) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            SERIALIZER.serialize(serializable, SerializerEncoding.JSON, stream);

            return stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public String getHeaderValue(String name) {
        return this.headers.getValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return new HttpHeaders(this.headers);
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        if (this.bodyBytes == null) {
            return Mono.empty();
        } else {
            return Mono.just(this.bodyBytes);
        }
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        if (this.bodyBytes == null) {
            return Flux.empty();
        } else {
            return Flux.just(ByteBuffer.wrap(this.bodyBytes));
        }
    }

    @Override
    public Mono<String> getBodyAsString() {
        return this.getBodyAsString(StandardCharsets.UTF_8);
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        if (this.bodyBytes == null) {
            return Mono.empty();
        } else {
            return Mono.just(new String(this.bodyBytes, charset));
        }
    }
}
