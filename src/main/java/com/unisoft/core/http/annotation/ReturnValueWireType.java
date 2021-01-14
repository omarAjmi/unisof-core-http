package com.unisoft.core.http.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for the type that will be used to deserialize the return value of a REST API response.
 * Supported values are:
 *
 * <ol>
 *     <li>{@link com.unisoft.core.util.Base64Url}</li>
 *     <li>{@link com.unisoft.core.util.DateTimeRfc1123}</li>
 *     <li>{@link com.unisoft.core.util.UnixTime}</li>
 *     <li>{@link java.util.List List&lt;T&gt;} where {@code T} can be one of the four values above.</li>
 * </ol>
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface ReturnValueWireType {
    /**
     * The type that the service interface method's return value will be converted from.
     *
     * @return The type that the service interface method's return value will be converted from.
     */
    Class<?> value();
}
