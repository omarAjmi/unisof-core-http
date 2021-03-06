package com.unisoft.core.http.serialize;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.unisoft.core.util.TypeUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author omar.H.Ajmi
 * @since 19/10/2020
 */
final class AdditionalPropertiesSerializer extends StdSerializer<Object> implements ResolvableSerializer {
    private static final long serialVersionUID = -3458779491516161716L;

    /**
     * The default mapperAdapter for the current type.
     */
    private final JsonSerializer<?> defaultSerializer;

    /**
     * The object mapper for default serializations.
     */
    private final ObjectMapper mapper;

    /**
     * Creates an instance of FlatteningSerializer.
     *
     * @param vc                handled type
     * @param defaultSerializer the default JSON serializer
     * @param mapper            the object mapper for default serializations
     */
    protected AdditionalPropertiesSerializer(Class<?> vc, JsonSerializer<?> defaultSerializer, ObjectMapper mapper) {
        super(vc, false);
        this.defaultSerializer = defaultSerializer;
        this.mapper = mapper;
    }

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson
     * ObjectMapper.
     *
     * @param mapper the object mapper for default serializations
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule(final ObjectMapper mapper) {
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc,
                                                      JsonSerializer<?> serializer) {
                for (Class<?> c : TypeUtil.getAllClasses(beanDesc.getBeanClass())) {
                    if (c.isAssignableFrom(Object.class)) {
                        continue;
                    }
                    Field[] fields = c.getDeclaredFields();
                    for (Field field : fields) {
                        if ("additionalProperties".equalsIgnoreCase(field.getName())) {
                            JsonProperty property = field.getAnnotation(JsonProperty.class);
                            if (property != null && property.value().isEmpty()) {
                                return new AdditionalPropertiesSerializer(beanDesc.getBeanClass(), serializer, mapper);
                            }
                        }
                    }
                }
                return serializer;
            }
        });
        return module;
    }

    @Override
    public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        ObjectNode root = mapper.valueToTree(value);
        ObjectNode res = root.deepCopy();
        Queue<ObjectNode> source = new LinkedBlockingQueue<ObjectNode>();
        Queue<ObjectNode> target = new LinkedBlockingQueue<ObjectNode>();
        source.add(root);
        target.add(res);
        while (!source.isEmpty()) {
            ObjectNode current = source.poll();
            ObjectNode resCurrent = target.poll();
            Iterator<Map.Entry<String, JsonNode>> fields = current.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode outNode = resCurrent.get(key);
                if ("additionalProperties".equals(key)) {
                    // Handle additional properties
                    resCurrent.remove(key);
                    // put each item back in
                    ObjectNode extraProperties = (ObjectNode) outNode;
                    Iterator<Map.Entry<String, JsonNode>> additionalFields = extraProperties.fields();
                    while (additionalFields.hasNext()) {
                        Map.Entry<String, JsonNode> additionalField = additionalFields.next();
                        resCurrent.put(additionalField.getKey(), additionalField.getValue());
                    }
                }
                if (field.getValue() instanceof ObjectNode) {
                    source.add((ObjectNode) field.getValue());
                    target.add((ObjectNode) outNode);
                } else if (field.getValue() instanceof ArrayNode
                        && (field.getValue()).size() > 0
                        && (field.getValue()).get(0) instanceof ObjectNode) {
                    Iterator<JsonNode> sourceIt = field.getValue().elements();
                    Iterator<JsonNode> targetIt = outNode.elements();
                    while (sourceIt.hasNext()) {
                        source.add((ObjectNode) sourceIt.next());
                        target.add((ObjectNode) targetIt.next());
                    }
                }
            }
        }
        jgen.writeTree(res);
    }

    @Override
    public void resolve(SerializerProvider provider) throws JsonMappingException {
        ((ResolvableSerializer) defaultSerializer).resolve(provider);
    }

    @Override
    public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider,
                                  TypeSerializer typeSerializer) throws IOException {
        serialize(value, gen, provider);
    }
}
