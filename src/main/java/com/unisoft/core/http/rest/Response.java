package com.unisoft.core.http.rest;

import com.unisoft.core.http.HttpHeaders;
import com.unisoft.core.http.HttpRequest;

/**
 * REST response with a strongly-typed content specified.
 *
 * @param <T> The deserialized type of the response content, available from {@link #getValue()}.
 * @author omar.H.Ajmi
 * @see ResponseBase
 * @since 18/10/2020
 */
public interface Response<T> {
    /**
     * Gets the HTTP response status code.
     *
     * @return The status code of the HTTP response.
     */
    int getStatusCode();

    /**
     * Gets the headers from the HTTP response.
     *
     * @return The HTTP response headers.
     */
    HttpHeaders getHeaders();

    /**
     * Gets the HTTP request which resulted in this response.
     *
     * @return The HTTP request.
     */
    HttpRequest getRequest();

    /**
     * Gets the deserialized value of the HTTP response.
     *
     * @return The deserialized value of the HTTP response.
     */
    T getValue();
}
