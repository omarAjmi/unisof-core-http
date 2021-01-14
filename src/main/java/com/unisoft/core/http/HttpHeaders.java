package com.unisoft.core.http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A collection of headers on an HTTP request or response.
 *
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
public class HttpHeaders implements Iterable<HttpHeader> {
    private final Map<String, HttpHeader> headers = new HashMap<>();

    /**
     * Create an empty HttpHeaders instance.
     */
    public HttpHeaders() {
    }

    /**
     * Create a HttpHeaders instance with the provided initial headers.
     *
     * @param headers the map of initial headers
     */
    public HttpHeaders(Map<String, String> headers) {
        for (final Map.Entry<String, String> header : headers.entrySet()) {
            this.put(header.getKey(), header.getValue());
        }
    }

    /**
     * Create a HttpHeaders instance with the provided initial headers.
     *
     * @param headers the collection of initial headers
     */
    public HttpHeaders(Iterable<HttpHeader> headers) {
        this();
        for (HttpHeader header : headers) {
            this.put(header.getName(), header.getValue());
        }
    }

    /**
     * Sets a {@link HttpHeader header} with the given name and value.
     *
     * <p>If header with same name already exists then the value will be overwritten.</p>
     *
     * @param name  the name
     * @param value the value
     * @return The updated HttpHeaders object
     */
    public HttpHeaders put(String name, String value) {
        this.headers.put(this.formatKey(name), new HttpHeader(name, value));
        return this;
    }

    /**
     * Gets the {@link HttpHeader header} for the provided header name. {@code Null} is returned if the header isn't
     * found.
     *
     * @param name the name of the header to find.
     * @return the header if found, null otherwise.
     */
    public HttpHeader get(String name) {
        return this.headers.get(this.formatKey(name));
    }

    /**
     * Removes the {@link HttpHeader header} with the provided header name. {@code Null} is returned if the header
     * isn't found.
     *
     * @param name the name of the header to remove.
     * @return the header if removed, null otherwise.
     */
    public HttpHeader remove(String name) {
        return this.headers.remove(this.formatKey(name));
    }

    private String formatKey(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    /**
     * Gets the number of headers in the collection.
     *
     * @return the number of headers in this collection.
     */
    public int getSize() {
        return this.headers.size();
    }

    /**
     * Gets a {@link Map} representation of the HttpHeaders collection.
     *
     * @return the headers as map
     */
    public Map<String, String> toMap() {
        Map<String, String> result = new HashMap<>();
        for (final Header header : this.headers.values()) {
            result.put(header.getName(), header.getValue());
        }
        return result;
    }

    /**
     * Get the value for the provided header name. {@code Null} is returned if the header name isn't found.
     *
     * @param name the name of the header whose value is being retrieved.
     * @return the value of the header, or null if the header isn't found
     */
    public String getValue(String name) {
        final HttpHeader httpHeader = this.get(name);
        return httpHeader == null ? null : httpHeader.getValue();
    }

    /**
     * Get the values for the provided header name. {@code Null} is returned if the header name isn't found.
     *
     * <p>This returns {@link #getValue(String) getValue} split by {@code comma}.</p>
     *
     * @param name the name of the header whose value is being retrieved.
     * @return the values of the header, or null if the header isn't found
     */
    public String[] getValues(String name) {
        final HttpHeader httpHeader = this.get(name);
        return httpHeader == null ? null : httpHeader.getValues();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<HttpHeader> iterator() {
        return this.headers.values().iterator();
    }

    /**
     * Get a {@link Stream} representation of the HttpHeader values in this instance.
     *
     * @return A {@link Stream} of all header values in this instance.
     */
    public Stream<HttpHeader> stream() {
        return this.headers.values().stream();
    }

    @Override
    public String toString() {
        return this.stream()
                .map(header -> header.getName() + "=" + header.getValue())
                .collect(Collectors.joining(", "));
    }
}
