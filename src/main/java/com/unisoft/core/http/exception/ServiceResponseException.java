package com.unisoft.core.http.exception;

import com.unisoft.core.exception.UnisoftException;

/**
 * A runtime exception indicating service response failure caused by one of the following scenarios:
 *
 * <ol>
 * <li>The request was sent, but the client failed to understand the response. (Not in the right format, partial
 * response, etc.).</li>
 * <li>The connection may have timed out. These errors can be retried for idempotent or safe operations.</li>
 * </ol>
 *
 * @author omar.H.Ajmi
 * @since 20/10/2020
 */
public class ServiceResponseException extends UnisoftException {
    /**
     * Initializes a new instance of the ServiceResponseException class.
     *
     * @param message the exception message or the response content if a message is not available
     */
    public ServiceResponseException(final String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the ServiceResponseException class.
     *
     * @param message the exception message.
     * @param cause   the Throwable which caused the creation of this ServiceResponseException.
     */
    public ServiceResponseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
