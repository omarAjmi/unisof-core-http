package com.unisoft.core.http.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.unisoft.core.util.Base64Url;

import java.io.IOException;

/**
 * @author omar.H.Ajmi
 * @since 19/10/2020
 */
final class Base64UrlSerializer extends JsonSerializer<Base64Url> {
    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson
     * ObjectMapper.
     *
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Base64Url.class, new Base64UrlSerializer());
        return module;
    }

    @Override
    public void serialize(Base64Url value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(value.toString());
    }
}
