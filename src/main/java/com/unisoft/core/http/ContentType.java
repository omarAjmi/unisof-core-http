package com.unisoft.core.http;

/**
 * The different values that commonly used for Content-Type header.
 *
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
public final class ContentType {
    /**
     * the default JSON Content-Type header.
     */
    public static final String APPLICATION_JSON = "application/json";

    /**
     * the default binary Content-Type header.
     */
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    /**
     * The default form data Content-Type header.
     */
    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    /**
     * Private ctr.
     */
    private ContentType() {
    }
}
