package com.unisoft.core.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpMethodTest {

    @Test
    void getTest() {
        assertEquals("GET", HttpMethod.GET.toString());
    }

    @Test
    void putTest() {
        assertEquals("PUT", HttpMethod.PUT.toString());
    }

    @Test
    void postTest() {
        assertEquals("POST", HttpMethod.POST.toString());
    }

    @Test
    void patchTest() {
        assertEquals("PATCH", HttpMethod.PATCH.toString());
    }

    @Test
    void deleteTest() {
        assertEquals("DELETE", HttpMethod.DELETE.toString());
    }

    @Test
    void headTest() {
        assertEquals("HEAD", HttpMethod.HEAD.toString());
    }
}