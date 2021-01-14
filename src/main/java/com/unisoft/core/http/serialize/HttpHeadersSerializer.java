package com.unisoft.core.http.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.unisoft.core.http.HttpHeaders;

import java.io.IOException;

/**
 * Custom serializer for serializing {@code HttpHeaders} objects into Base64 strings.
 *
 * @author omar.H.Ajmi
 * @since 19/10/2020
 */
final class HttpHeadersSerializer extends JsonSerializer<HttpHeaders> {
    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson
     * ObjectMapper.
     *
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(HttpHeaders.class, new HttpHeadersSerializer());
        return module;
    }

    @Override
    public void serialize(HttpHeaders value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeObject(value.toMap());
    }
}
