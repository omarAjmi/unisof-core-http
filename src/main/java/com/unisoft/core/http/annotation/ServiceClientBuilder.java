package com.unisoft.core.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation given to all service client builder classes.
 *
 * @author omar.H.Ajmi
 * @since 17/01/2021
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServiceClientBuilder {

    /**
     * An array of classes that this builder can build.
     *
     * @return An array of all classnames that this builder can create an instance of.
     */
    Class<?>[] serviceClients();

    /**
     * The {@link ServiceClientProtocol protocol} clients created from this builder will use to interact with the
     * service.
     *
     * @return The {@link ServiceClientProtocol}.
     */
    ServiceClientProtocol protocol() default ServiceClientProtocol.HTTP;
}