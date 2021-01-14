package com.unisoft.core.http.rest;

import com.unisoft.core.http.HttpHeaders;
import com.unisoft.core.http.HttpRequest;

/**
 * REST response with a strongly-typed content specified.
 *
 * @param <T> The deserialized type of the response content.
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
public class SimpleResponse<T> implements Response<T> {
    private final HttpRequest request;
    private final int statusCode;
    private final HttpHeaders headers;
    private final T value;

    /**
     * Creates a {@link SimpleResponse}.
     *
     * @param request    The request which resulted in this response.
     * @param statusCode The status code of the HTTP response.
     * @param headers    The headers of the HTTP response.
     * @param value      The deserialized value of the HTTP response.
     */
    public SimpleResponse(HttpRequest request, int statusCode, HttpHeaders headers, T value) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.value = value;
    }

    /**
     * Gets the status code of the HTTP response.
     *
     * @return The status code of the HTTP response.
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
     * Gets the request which resulted in this {@link SimpleResponse}.
     *
     * @return The request which resulted in this {@link SimpleResponse}.
     */
    @Override
    public HttpRequest getRequest() {
        return this.request;
    }

    /**
     * Gets the deserialized value of the HTTP response.
     *
     * @return The deserialized value of the HTTP response.
     */
    @Override
    public T getValue() {
        return this.value;
    }
}
