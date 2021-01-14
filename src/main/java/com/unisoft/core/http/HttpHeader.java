package com.unisoft.core.http;

/**
 * A single header within a HTTP request or response.
 * <p>
 * If multiple header values are added to a HTTP request or response with
 * the same name (case-insensitive), then the values will be appended
 * to the end of the same Header with commas separating them.
 *
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
public class HttpHeader extends Header {
    /**
     * Create a HttpHeader instance using the provided name and value.
     *
     * @param name  the name
     * @param value the value
     */
    public HttpHeader(String name, String value) {
        super(name, value);
    }
}
