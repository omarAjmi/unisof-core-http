package com.unisoft.core.http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class HeaderTest {
    private static Stream<Arguments> testNullArgsConstructor() {
        return Stream.of(
                Arguments.arguments(null, "a"),
                Arguments.arguments(null, null)
        );
    }

    @Test
    void testAddValue() {
        // Arrange
        final Header header = new Header("a", "b");
        // Act
        header.addValue("c");

        // Assert
        assertEquals("a:b,c", header.toString());
    }

    @ParameterizedTest
    @MethodSource
    void testNullArgsConstructor(String name, String value) {
        // Arrange, Act & Assert
        assertThrows(NullPointerException.class, () -> new Header(name, value));
    }

    @Test
    void testNameValue() {
        // Arrange
        String name = "a";
        String value = "b";

        // Act
        final Header header = new Header(name, value);

        // Assert
        assertEquals(value, header.getValue());
        assertEquals(name, header.getName());
    }

    @Test
    void testGetValues() {
        // Arrange
        String name = "a";
        String[] values = {"b", "c"};

        // Act
        final Header header = new Header(name, values[0]);
        header.addValue(values[1]);

        // Assert
        assertArrayEquals(values, header.getValues());
    }
}