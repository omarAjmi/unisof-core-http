package com.unisoft.core.http.exception;

import com.unisoft.core.http.HttpResponse;

/**
 * An error response, typically triggered by a 412 response (for update) or 404 (for get/post)
 *
 * @author omar.H.Ajmi
 * @since 20/10/2020
 */
public class ResourceNotFoundException extends HttpResponseException {
    /**
     * Initializes a new instance of the ResourceNotFoundException class.
     *
     * @param message  the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public ResourceNotFoundException(final String message, final HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the ResourceNotFoundException class.
     *
     * @param message  the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param value    the deserialized response value
     */
    public ResourceNotFoundException(final String message, final HttpResponse response, final Object value) {
        super(message, response, value);
    }


    /**
     * Initializes a new instance of the ResourceNotFoundException class.
     *
     * @param message  the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause    the Throwable which caused the creation of this ResourceNotFoundException
     */
    public ResourceNotFoundException(final String message, final HttpResponse response, final Throwable cause) {
        super(message, response, cause);
    }
}
