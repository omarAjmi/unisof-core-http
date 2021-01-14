package com.unisoft.core.http.serialize.impl;

/**
 * An exception thrown while parsing an invalid input during serialization or deserialization.
 *
 * @author omar.H.Ajmi
 * @since 19/10/2020
 */
public class MalformedValueException extends RuntimeException {
    /**
     * Create a MalformedValueException instance.
     *
     * @param message the exception message
     */
    public MalformedValueException(String message) {
        super(message);
    }

    /**
     * Create a MalformedValueException instance.
     *
     * @param message the exception message
     * @param cause   the actual cause
     */
    public MalformedValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
