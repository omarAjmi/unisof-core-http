package com.unisoft.core.http.annotation;

import com.unisoft.core.http.exception.HttpResponseException;

import java.lang.annotation.*;

/**
 * @author omar.H.Ajmi
 * @since 16/01/2021
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Repeatable(UnexpectedResponseExceptionTypes.class)
public @interface UnexpectedResponseExceptionType {
    Class<? extends HttpResponseException> value();

    int[] code() default {};
}
