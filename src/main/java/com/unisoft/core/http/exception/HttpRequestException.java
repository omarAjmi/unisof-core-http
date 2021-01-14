package com.unisoft.core.http.exception;

import com.unisoft.core.exception.UnisoftException;
import com.unisoft.core.http.HttpResponse;

/**
 * The exception when an HTTP request fails.
 * <p>
 * Generally, these errors are safe to retry.
 *
 * @author omar.H.Ajmi
 * @since 19/10/2020
 */
public class HttpRequestException extends UnisoftException {
    /**
     * The HTTP response value.
     */
    private final Object value;

    /**
     * Information about the associated HTTP response.
     */
    private final HttpResponse response;

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param response The {@link HttpResponse} received that is associated to the exception.
     */
    public HttpRequestException(final HttpResponse response) {
        super();
        this.value = null;
        this.response = response;
    }

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param message  The exception message.
     * @param response The {@link HttpResponse} received that is associated to the exception.
     */
    public HttpRequestException(final String message, final HttpResponse response) {
        super(message);
        this.value = null;
        this.response = response;
    }

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param response The {@link HttpResponse} received that is associated to the exception.
     * @param cause    The {@link Throwable} which caused the creation of this exception.
     */
    public HttpRequestException(final HttpResponse response, final Throwable cause) {
        super(cause);
        this.value = null;
        this.response = response;
    }

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param message  The exception message.
     * @param response The {@link HttpResponse} received that is associated to the exception.
     * @param value    The deserialized response value.
     */
    public HttpRequestException(final String message, final HttpResponse response, final Object value) {
        super(message);
        this.value = value;
        this.response = response;
    }

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param message  The exception message.
     * @param response The {@link HttpResponse} received that is associated to the exception.
     * @param cause    The {@link Throwable} which caused the creation of this exception.
     */
    public HttpRequestException(final String message, final HttpResponse response, final Throwable cause) {
        super(message, cause);
        this.value = null;
        this.response = response;
    }

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param message            The exception message.
     * @param response           The {@link HttpResponse} received that is associated to the exception.
     * @param cause              The {@link Throwable} which caused the creation of this exception.
     * @param enableSuppression  Whether suppression is enabled or disabled.
     * @param writableStackTrace Whether the exception stack trace will be filled in.
     */
    public HttpRequestException(final String message, final HttpResponse response, final Throwable cause,
                                final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.value = null;
        this.response = response;
    }

    /**
     * @return The {@link HttpResponse} received that is associated to the exception.
     */
    public HttpResponse getResponse() {
        return response;
    }

    /**
     * @return The deserialized HTTP response value.
     */
    public Object getValue() {
        return value;
    }
}
