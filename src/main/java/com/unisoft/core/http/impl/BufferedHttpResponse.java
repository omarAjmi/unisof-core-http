package com.unisoft.core.http.impl;

import com.unisoft.core.http.HttpHeaders;
import com.unisoft.core.http.HttpResponse;
import com.unisoft.core.util.CoreUtil;
import com.unisoft.core.util.FluxUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * HTTP response which will buffer the response's body when/if it is read.
 *
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
public final class BufferedHttpResponse extends HttpResponse {
    private final HttpResponse innerHttpResponse;
    private final Flux<ByteBuffer> cachedBody;

    public BufferedHttpResponse(HttpResponse httpResponse) {
        super(httpResponse.getRequest());
        this.innerHttpResponse = httpResponse;
        this.cachedBody = FluxUtil.collectBytesInByteBufferStream(httpResponse.getBody())
                .map(ByteBuffer::wrap)
                .flux()
                .cache();
    }

    @Override
    public int getStatusCode() {
        return this.innerHttpResponse.getStatusCode();
    }

    @Override
    public String getHeaderValue(String name) {
        return this.innerHttpResponse.getHeaderValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.innerHttpResponse.getHeaders();
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return this.cachedBody;
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return this.cachedBody.next().map(ByteBuffer::array);
    }

    @Override
    public Mono<String> getBodyAsString() {
        return this.getBodyAsByteArray()
                .map(bytes -> CoreUtil.bomAwareToString(bytes, this.innerHttpResponse.getHeaderValue("content-type")));
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return this.getBodyAsByteArray()
                .map(bytes -> bytes == null ? null : new String(bytes, charset));
    }

    @Override
    public HttpResponse buffer() {
        return this;
    }
}
