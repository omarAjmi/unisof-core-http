package com.unisoft.core.http.util;

/**
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
class UrlToken {
    private final String text;
    private final UrlTokenType type;

    UrlToken(String text, UrlTokenType type) {
        this.text = text;
        this.type = type;
    }

    static UrlToken scheme(String text) {
        return new UrlToken(text, UrlTokenType.SCHEME);
    }

    static UrlToken host(String text) {
        return new UrlToken(text, UrlTokenType.HOST);
    }

    static UrlToken port(String text) {
        return new UrlToken(text, UrlTokenType.PORT);
    }

    static UrlToken path(String text) {
        return new UrlToken(text, UrlTokenType.PATH);
    }

    static UrlToken query(String text) {
        return new UrlToken(text, UrlTokenType.QUERY);
    }

    String text() {
        return this.text;
    }

    UrlTokenType type() {
        return this.type;
    }

    @Override
    public boolean equals(Object rhs) {
        return rhs instanceof UrlToken && equals((UrlToken) rhs);
    }

    public boolean equals(UrlToken rhs) {
        return rhs != null && this.text.equals(rhs.text) && type == rhs.type;
    }

    @Override
    public String toString() {
        return "\"" + this.text + "\" (" + this.type + ")";
    }

    @Override
    public int hashCode() {
        return (this.text == null ? 0 : this.text.hashCode()) ^ this.type.hashCode();
    }
}
