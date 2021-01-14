package com.unisoft.core.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used for flattening properties separated by '.'.
 * E.g. a property with JsonProperty value "properties.value"
 * will have "value" property under the "properties" tree on
 * the wire.
 *
 * @author omar.H.Ajmi
 * @since 19/10/2020
 */
@Retention(RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
public @interface JsonFlatten {
}
