package com.unisoft.core.http.client;

import java.time.Duration;

/**
 * @author omar.H.Ajmi
 * @since 23/10/2020
 */
public interface ServiceClientOptionsBuilder {

    /**
     * builds client options
     *
     * @return {@code ServiceClientOptions}
     */
    ServiceClientOptions build();

    /**
     * sets max retries
     *
     * @param maxRetry
     * @return {@code ServiceClientOptionsBuilder}
     */
    ServiceClientOptionsBuilder maxRetry(int maxRetry);

    /**
     * sets client request timout
     *
     * @param timeout
     * @return {@code ServiceClientOptionsBuilder}
     */
    ServiceClientOptionsBuilder timeout(Duration timeout);

    /**
     * sets client base baseUrl
     *
     * @param baseUrl
     * @return {@code ServiceClientOptionsBuilder}
     */
    ServiceClientOptionsBuilder baseUrl(String baseUrl);
}
