package com.unisoft.core.http.serialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonAdapterTest {

    private static Stream<Arguments> deserializeJsonSupplier() {
        final String jsonFormat = "{\"OffsetDateTime\":\"%s\"}";
        OffsetDateTime minValue = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime unixEpoch = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        return Stream.of(
                Arguments.of(String.format(jsonFormat, "0001-01-01T00:00:00"), minValue),
                Arguments.of(String.format(jsonFormat, "0001-01-01T00:00:00Z"), minValue),
                Arguments.of(String.format(jsonFormat, "1970-01-01T00:00:00"), unixEpoch),
                Arguments.of(String.format(jsonFormat, "1970-01-01T00:00:00Z"), unixEpoch)
        );
    }

    private static Stream<Arguments> deserializeXmlSupplier() {
        final String xmlFormat = "<Wrapper><OffsetDateTime>%s</OffsetDateTime></Wrapper>";
        OffsetDateTime minValue = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime unixEpoch = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        return Stream.of(
                Arguments.of(String.format(xmlFormat, "0001-01-01T00:00:00"), minValue),
                Arguments.of(String.format(xmlFormat, "0001-01-01T00:00:00Z"), minValue),
                Arguments.of(String.format(xmlFormat, "1970-01-01T00:00:00"), unixEpoch),
                Arguments.of(String.format(xmlFormat, "1970-01-01T00:00:00Z"), unixEpoch)
        );
    }

    @Test
    void emptyMap() throws IOException {
        final Map<String, String> map = new HashMap<>();
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{}", serializer.serialize(map, SerializerEncoding.JSON));
    }

    @Test
    void mapWithNullKey() throws IOException {
        final Map<String, String> map = new HashMap<>();
        map.put(null, null);
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{}", serializer.serialize(map, SerializerEncoding.JSON));
    }

    @Test
    void mapWithEmptyKeyAndNullValue() throws IOException {
        final MapHolder mapHolder = new MapHolder();
        mapHolder.map(new HashMap<>());
        mapHolder.map().put("", null);

        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{\"map\":{\"\":null}}", serializer.serialize(mapHolder, SerializerEncoding.JSON));
    }

    @Test
    void mapWithEmptyKeyAndEmptyValue() throws IOException {
        final MapHolder mapHolder = new MapHolder();
        mapHolder.map = new HashMap<>();
        mapHolder.map.put("", "");
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{\"map\":{\"\":\"\"}}", serializer.serialize(mapHolder, SerializerEncoding.JSON));
    }

    @Test
    void mapWithEmptyKeyAndNonEmptyValue() throws IOException {
        final Map<String, String> map = new HashMap<>();
        map.put("", "test");
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{\"\":\"test\"}", serializer.serialize(map, SerializerEncoding.JSON));
    }

    @ParameterizedTest
    @MethodSource("deserializeJsonSupplier")
    public void deserializeJson(String json, OffsetDateTime expected) throws IOException {
        DateTimeWrapper wrapper = JacksonAdapter.createDefaultSerializerAdapter()
                .deserialize(json, DateTimeWrapper.class, SerializerEncoding.JSON);

        assertEquals(expected, wrapper.getOffsetDateTime());
    }

    @ParameterizedTest
    @MethodSource("deserializeXmlSupplier")
    public void deserializeXml(String xml, OffsetDateTime expected) throws IOException {
        DateTimeWrapper wrapper = JacksonAdapter.createDefaultSerializerAdapter()
                .deserialize(xml, DateTimeWrapper.class, SerializerEncoding.XML);

        assertEquals(expected, wrapper.getOffsetDateTime());
    }

    private static class MapHolder {
        @JsonInclude(content = JsonInclude.Include.ALWAYS)
        private Map<String, String> map = new HashMap<>();

        public Map<String, String> map() {
            return map;
        }

        public void map(Map<String, String> map) {
            this.map = map;
        }
    }

    @JacksonXmlRootElement(localName = "XmlString")
    private static class XmlString {
        @JsonProperty("Value")
        private String value;

        public String getValue() {
            return value;
        }
    }

    @JacksonXmlRootElement(localName = "Wrapper")
    private static class DateTimeWrapper {
        @JsonProperty(value = "OffsetDateTime", required = true)
        private OffsetDateTime offsetDateTime;

        public OffsetDateTime getOffsetDateTime() {
            return offsetDateTime;
        }

        public DateTimeWrapper setOffsetDateTime(OffsetDateTime offsetDateTime) {
            this.offsetDateTime = offsetDateTime;
            return this;
        }
    }
}