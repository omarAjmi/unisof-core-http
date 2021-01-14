package com.unisoft.core.http.rest;

import com.unisoft.core.http.HttpHeaders;
import com.unisoft.core.http.HttpRequest;

/**
 * The response of a REST request.
 *
 * @param <H> The deserialized type of the response headers.
 * @param <T> The deserialized type of the response value, available from {@link Response#getValue()}.
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
public class ResponseBase<H, T> implements Response<T> {

    private final HttpRequest request;
    private final int statusCode;
    private final H deserializedHeaders;
    private final HttpHeaders headers;
    private final T value;

    /**
     * Creates a {@link ResponseBase}.
     *
     * @param request             The HTTP request which resulted in this response.
     * @param statusCode          The status code of the HTTP response.
     * @param headers             The headers of the HTTP response.
     * @param deserializedHeaders The deserialized headers of the HTTP response.
     * @param value               The deserialized value of the HTTP response.
     */
    public ResponseBase(HttpRequest request, int statusCode, H deserializedHeaders, HttpHeaders headers, T value) {
        this.request = request;
        this.statusCode = statusCode;
        this.deserializedHeaders = deserializedHeaders;
        this.headers = headers;
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpHeaders getHeaders() {
        return this.headers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpRequest getRequest() {
        return this.request;
    }

    /**
     * Get the headers from the HTTP response, transformed into the header type, {@code H}.
     *
     * @return An instance of header type {@code H}, deserialized from the HTTP response headers.
     */
    public H getDeserializedHeaders() {
        return deserializedHeaders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getValue() {
        return this.value;
    }
}
