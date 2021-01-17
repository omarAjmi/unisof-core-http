package com.unisoft.core.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author omar.H.Ajmi
 * @since 16/01/2021
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface QueryParam {
    String value();

    boolean encoded() default false;
}
