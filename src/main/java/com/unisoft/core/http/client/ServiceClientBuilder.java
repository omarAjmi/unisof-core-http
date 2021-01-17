package com.unisoft.core.http.client;

import com.unisoft.core.http.HttpPipeline;
import com.unisoft.core.http.serialize.SerializerAdapter;

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

    ServiceClientBuilder serializerAdapter(SerializerAdapter serializerAdapter);

    ServiceClientBuilder httpPipeline(HttpPipeline httpPipeline);
}
