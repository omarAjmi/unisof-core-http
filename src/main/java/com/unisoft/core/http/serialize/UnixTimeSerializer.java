package com.unisoft.core.http.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.unisoft.core.util.UnixTime;

import java.io.IOException;

/**
 * Custom serializer for serializing {@link UnixTime} object into epoch formats.
 *
 * @author omar.H.Ajmi
 * @since 19/10/2020
 */
final class UnixTimeSerializer extends JsonSerializer<UnixTime> {
    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson
     * ObjectMapper.
     *
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(UnixTime.class, new UnixTimeSerializer());
        return module;
    }

    @Override
    public void serialize(UnixTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeNumber(value.toString());
    }
}
