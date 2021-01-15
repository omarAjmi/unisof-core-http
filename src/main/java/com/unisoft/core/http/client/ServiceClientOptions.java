package com.unisoft.core.http.client;

import java.time.Duration;

/**
 * service client options holder
 *
 * @author omar.H.Ajmi
 * @since 23/10/2020
 */
public class ServiceClientOptions {

    private final int maxRetry;
    private final Duration timeout;
    private final String endpoint;

    ServiceClientOptions(int maxRetry, Duration timeout, String endpoint) {
        this.maxRetry = maxRetry;
        this.timeout = timeout;
        this.endpoint = endpoint;
    }

    public int getMaxRetry() {
        return this.maxRetry;
    }

    public Duration getTimeout() {
        return this.timeout;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * builder for {@code ServiceClientOptions}
     */
    public static class Builder implements ServiceClientOptionsBuilder {

        private int maxRetry;
        private Duration timeout;
        private String endpoint;

        @Override
        public ServiceClientOptions build() {
            return new ServiceClientOptions(this.maxRetry, this.timeout, this.endpoint);
        }

        @Override
        public Builder maxRetry(int maxRetry) {
            this.maxRetry = maxRetry;
            return this;
        }

        @Override
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        @Override
        public Builder baseUrl(String baseUrl) {
            this.endpoint = baseUrl;
            return this;
        }
    }
}
