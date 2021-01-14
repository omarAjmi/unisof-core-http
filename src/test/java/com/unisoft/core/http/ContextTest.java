package com.unisoft.core.http;

import com.unisoft.core.util.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ContextTest {
    private static Stream<Arguments> addDataSupplier() {
        return Stream.of(
                // Adding with same key overwrites value.
                Arguments.of("key", "newValue", "newValue"),
                Arguments.of("key", "", ""),

                // New values.
                Arguments.of("key2", "newValue", "value"),
                Arguments.of("key2", "", "value")
        );
    }

    private static Stream<Arguments> getValuesSupplier() {
        Context contextWithMultipleKeys = new Context("key", "value")
                .addData("key2", "value2");
        Map<Object, Object> expectedMultipleKeys = new HashMap<>();
        expectedMultipleKeys.put("key", "value");
        expectedMultipleKeys.put("key2", "value2");

        Context contextWithMultipleSameKeys = new Context("key", "value")
                .addData("key", "value2");

        return Stream.of(
                Arguments.of(Context.NONE, Collections.emptyMap()),
                Arguments.of(new Context("key", "value"), Collections.singletonMap("key", "value")),
                Arguments.of(contextWithMultipleKeys, expectedMultipleKeys),
                Arguments.of(contextWithMultipleSameKeys, Collections.singletonMap("key", "value2"))
        );
    }

    private static Stream<Arguments> getDataAsSupplier() {
        return Stream.of(
                Arguments.of(Integer.class, 0),
                Arguments.of(String.class, "fooBar"),
                Arguments.of(ContextTest.class, new ContextTest())
        );
    }

    @Test
    void simpleContext() {
        Context context = new Context("key", "value");

        assertEquals("value", context.getData("key").orElse(""));
        assertFalse(context.getData("fakeKey").isPresent());
    }

    @Test
    void constructorKeyCannotBeNull() {
        assertThrows(NullPointerException.class, () -> new Context(null, null));
    }

    @ParameterizedTest
    @MethodSource("addDataSupplier")
    void addData(String key, String value, String expectedOriginalValue) {
        Context context = new Context("key", "value").addData(key, value);

        assertEquals(value, context.getData(key).orElse(""));
        assertEquals(expectedOriginalValue, context.getData("key").orElse(""));
    }

    @Test
    void addDataKeyCannotBeNull() {
        Context context = new Context("key", "value");

        assertThrows(IllegalArgumentException.class, () -> context.addData(null, null));
    }

    @Test
    void of() {
        Context context = Context.of(Collections.singletonMap("key", "value"));

        assertEquals("value", context.getData("key").orElse(""));

        Map<Object, Object> complexValues = new HashMap<>();
        complexValues.put("key", "value");
        complexValues.put("key2", "value2");
        context = Context.of(complexValues);

        assertEquals("value", context.getData("key").orElse(""));
        assertEquals("value2", context.getData("key2").orElse(""));
    }

    @Test
    void ofValuesCannotBeNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> Context.of(null));
        assertThrows(IllegalArgumentException.class, () -> Context.of(Collections.emptyMap()));
    }

    @Test
    void getValueKeyCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> Context.NONE.getData(null));
    }

    @ParameterizedTest
    @MethodSource("getValuesSupplier")
    void getValues(Context context, Map<Object, Object> expected) {
        assertEquals(expected, context.getValues());
    }

    @ParameterizedTest
    @MethodSource("getDataAsSupplier")
    void getData(Class<Object> type, Object value) {
        final Context context = new Context("key", value);
        context.getData("key", type)
                .ifPresent(existingValue -> assertEquals(type, existingValue.getClass()));
    }

    @Test
    void transformToString() {
        final Context context = new Context("key", "value");
        assertEquals("context{key=key, value=value}", context.toString());
        assertEquals("context{key=secondKey, value=secondValue, parent=" + context.toString() + "}", context.addData("secondKey", "secondValue").toString());
    }
}
