package com.unisoft.core.http.client;

import java.util.Objects;

/**
 * base service client object
 * should be extended whenever a service client implementation is needed
 *
 * @author omar.H.Ajmi
 * @since 27/10/2020
 */
public abstract class ServiceClient {
    private final ServiceClientOptions options;

    protected ServiceClient(ServiceClientOptions options) {
        this.options = Objects.requireNonNull(options, "'options' cannot be null");
    }

    public ServiceClientOptions getOptions() {
        return options;
    }

    /**
     * builder class for {@code AbstractServiceClient}
     */
    public static abstract class Builder implements ServiceClientBuilder {

        private ServiceClientOptions options;

        @Override
        public Builder options(ServiceClientOptions options) {
            this.options = options;
            return this;
        }

        public ServiceClientOptions getOptions() {
            return options;
        }
    }
}
