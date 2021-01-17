package com.unisoft.core.http.annotation;

/**
 * Enumeration of protocols available for setting the {@link ServiceClientBuilder#protocol() protocol} property of
 * {@link ServiceClientBuilder} annotation.
 *
 * @author omar.H.Ajmi
 * @since 16/01/2021
 */
public enum ServiceClientProtocol {
    HTTP,
    AMQP;

    ServiceClientProtocol() {
    }
}
