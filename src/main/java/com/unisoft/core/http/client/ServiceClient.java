package com.unisoft.core.http.client;

import com.unisoft.core.http.HttpPipeline;
import com.unisoft.core.http.serialize.SerializerAdapter;
import com.unisoft.core.util.Context;

import java.util.Objects;

/**
 * base service client object
 * should be extended whenever a service client implementation is needed
 * ServiceClient is the abstraction for accessing REST operations and their payload data types.
 *
 * @author omar.H.Ajmi
 * @since 27/10/2020
 */
public abstract class ServiceClient {
    private final ServiceClientOptions options;
    private final SerializerAdapter serializerAdapter;
    private final HttpPipeline httpPipeline;
    private final String sdkName;

    protected ServiceClient(ServiceClientOptions options, SerializerAdapter serializerAdapter, HttpPipeline httpPipeline) {
        this.options = Objects.requireNonNull(options, "'options' cannot be null");
        this.serializerAdapter = serializerAdapter;
        this.httpPipeline = httpPipeline;
        String packageName = this.getClass().getPackage().getName();
        String implementationSegment = ".impl";
        if (packageName.endsWith(implementationSegment)) {
            packageName = packageName.substring(0, packageName.length() - implementationSegment.length());
        }
        this.sdkName = packageName;
    }

    public ServiceClientOptions getOptions() {
        return options;
    }

    public SerializerAdapter getSerializerAdapter() {
        return serializerAdapter;
    }

    public HttpPipeline getHttpPipeline() {
        return httpPipeline;
    }

    /**
     * Gets default client context.
     *
     * @return the default client context.
     */
    public Context getContext() {
        return new Context("Sdk-Name", this.sdkName);
    }

    /**
     * builder class for {@code AbstractServiceClient}
     */
    public static abstract class Builder implements ServiceClientBuilder {

        private ServiceClientOptions options;
        private SerializerAdapter serializerAdapter;
        private HttpPipeline httpPipeline;

        @Override
        public Builder options(ServiceClientOptions options) {
            this.options = options;
            return this;
        }

        @Override
        public Builder serializerAdapter(SerializerAdapter serializerAdapter) {
            this.serializerAdapter = serializerAdapter;
            return this;
        }

        @Override
        public Builder httpPipeline(HttpPipeline httpPipeline) {
            this.httpPipeline = httpPipeline;
            return this;
        }

        public ServiceClientOptions getOptions() {
            return this.options;
        }

        public SerializerAdapter getSerializerAdapter() {
            return this.serializerAdapter;
        }

        public HttpPipeline getHttpPipeline() {
            return this.httpPipeline;
        }
    }
}
