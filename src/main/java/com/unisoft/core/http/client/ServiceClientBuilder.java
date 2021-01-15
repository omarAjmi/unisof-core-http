package com.unisoft.core.http.client;

/**
 * service client builder contract
 *
 * @author omar.H.Ajmi
 * @since 23/10/2020
 */
public interface ServiceClientBuilder {
    /**
     * instantiate a service client
     *
     * @return a service client
     */
    ServiceClient build();

    ServiceClientBuilder options(ServiceClientOptions options);
}
