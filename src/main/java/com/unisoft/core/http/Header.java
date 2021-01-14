package com.unisoft.core.http;

import java.util.Objects;

/**
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
public class Header {
    private final String name;
    private String value;

    /**
     * Create a Header instance using the provided name and value.
     *
     * @param name  the name of the header.
     * @param value the value of the header.
     * @throws NullPointerException if {@code name} is null.
     */
    public Header(String name, String value) {
        Objects.requireNonNull(name, "'name' cannot be null");
        this.name = name;
        this.value = value;
    }

    /**
     * Gets the header name.
     *
     * @return the name of this {@link Header}
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the value of this {@link Header}.
     *
     * @return the value of this Header
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Gets the comma separated value as an array.
     *
     * @return the values of this {@link Header} that are separated by a comma
     */
    public String[] getValues() {
        return this.value == null ? null : this.value.split(",");
    }

    /**
     * Add a new value to the end of the Header.
     *
     * @param value the value to add
     */
    public void addValue(String value) {
        if (Objects.isNull(this.value)) {
            this.value = value;
        } else {
            this.value += "," + value;
        }
    }

    /**
     * Gets the String representation of the header.
     *
     * @return the String representation of this Header.
     */
    @Override
    public String toString() {
        return this.name + ":" + this.value;
    }
}
