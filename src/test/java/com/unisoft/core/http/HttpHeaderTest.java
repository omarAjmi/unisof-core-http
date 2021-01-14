package com.unisoft.core.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpHeaderTest {

    @Test
    void addValue() {
        final Header header = new HttpHeader("a", "b");
        header.addValue("c");
        assertEquals("b,c", header.getValue());
    }
}