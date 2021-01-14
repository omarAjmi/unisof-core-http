package com.unisoft.core.http.rest;

import com.unisoft.core.http.HttpHeaders;
import com.unisoft.core.http.HttpRequest;
import reactor.core.publisher.Flux;

import java.io.Closeable;
import java.nio.ByteBuffer;

/**
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
public final class StreamResponse extends SimpleResponse<Flux<ByteBuffer>> implements Closeable {
    private volatile boolean consumed;

    /**
     * Creates a {@link StreamResponse}.
     *
     * @param request    The request which resulted in this response.
     * @param statusCode The status code of the HTTP response.
     * @param headers    The headers of the HTTP response.
     * @param value      The content of the HTTP response.
     */
    public StreamResponse(HttpRequest request, int statusCode, HttpHeaders headers, Flux<ByteBuffer> value) {
        super(request, statusCode, headers, value);
    }

    /**
     * The content of the HTTP response as a stream of {@link ByteBuffer byte buffers}.
     *
     * @return The content of the HTTP response as a stream of {@link ByteBuffer byte buffers}.
     */
    @Override
    public Flux<ByteBuffer> getValue() {
        return super.getValue().doFinally(t -> this.consumed = true);
    }

    /**
     * Disposes the connection associated with this {@link StreamResponse}.
     */
    @Override
    public void close() {
        if (this.consumed) {
            return;
        }
        this.consumed = true;
        final Flux<ByteBuffer> value = getValue();
        value.subscribe().dispose();
    }
}
