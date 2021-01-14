package com.unisoft.core.http.serialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents a generic Java type, retaining information about generics.
 *
 * @param <T> The type being represented.
 * @author omar.H.Ajmi
 * @since 19/10/2020
 */
public abstract class TypeReference<T> {

    private static final Logger log = LoggerFactory.getLogger(TypeReference.class);
    private static final String MISSING_TYPE = "Type constructed without type information.";

    private static final Map<Class<?>, TypeReference<?>> CACHE = new ConcurrentHashMap<>();

    private final Type javaType;

    /**
     * Constructs a new {@link TypeReference} which maintains generic information.
     *
     * @throws IllegalArgumentException If the reference is constructed without type information.
     */
    public TypeReference() {
        Type superClass = this.getClass().getGenericSuperclass();
        if (superClass instanceof Class) {
            throw new IllegalArgumentException(MISSING_TYPE);
        } else {
            this.javaType = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        }
    }

    private TypeReference(Class<T> clazz) {
        this.javaType = clazz;
    }

    /**
     * Creates and instance of {@link TypeReference} which maintains the generic {@code T} of the passed {@link Class}.
     * <p>
     * This method will cache the instance of {@link TypeReference} using the passed {@link Class} as the key. This is
     * meant to be used with non-generic types such as primitive object types and POJOs, not {@code Map<String, Object>}
     * or {@code List<Integer>} parameterized types.
     *
     * @param clazz {@link Class} that contains generic information used to create the {@link TypeReference}.
     * @param <T>   The generic type.
     * @return Either the cached or new instance of {@link TypeReference}.
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeReference<T> createInstance(Class<T> clazz) {
        /*
         * When computing the TypeReference if the key is absent ignore the parameter from the compute function. The
         * compute function wildcards to T type which causes the type system to breakdown.
         */
        return (TypeReference<T>) CACHE.computeIfAbsent(clazz, c -> new TypeReference<T>(clazz) {
        });
    }

    /**
     * Returns the {@link Type} representing {@code T}.
     *
     * @return The {@link Type} representing {@code T}.
     */
    public Type getJavaType() {
        return this.javaType;
    }
}
