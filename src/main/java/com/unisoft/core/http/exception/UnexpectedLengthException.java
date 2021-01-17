package com.unisoft.core.http.exception;

/**
 * This exception class represents an error when the specified input length doesn't match the data length.
 *
 * @author omar.H.Ajmi
 * @since 16/01/2021
 */
public class UnexpectedLengthException extends Throwable {
    private final long bytesRead;
    private final long bytesExpected;

    /**
     * Constructor of the UnexpectedLengthException.
     *
     * @param message       The message for the exception.
     * @param bytesRead     The number of bytes read from resource.
     * @param bytesExpected The number of bytes expected from the receiver.
     */
    public UnexpectedLengthException(String message, long bytesRead, long bytesExpected) {
        super(message);
        this.bytesRead = bytesRead;
        this.bytesExpected = bytesExpected;
    }

    /**
     * @return the number of bytes read from the input
     */
    public long getBytesRead() {
        return this.bytesRead;
    }

    /**
     * @return the number of bytes that were expected to be read from the input
     */
    public long getBytesExpected() {
        return this.bytesExpected;
    }
}
