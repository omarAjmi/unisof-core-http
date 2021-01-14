package com.unisoft.core.http;

/**
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
@FunctionalInterface
public interface HttpClientProvider {

    /**
     * Creates a new instance of the {@link HttpClient} that this HttpClientProvider is configured to create.
     *
     * @returnA new {@link HttpClient} instance, entirely unrelated to all other instances that were created
     * previously.
     */
    HttpClient createInstance();
}
