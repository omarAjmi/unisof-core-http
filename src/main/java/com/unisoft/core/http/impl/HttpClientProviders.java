package com.unisoft.core.http.impl;

import com.unisoft.core.http.HttpClient;
import com.unisoft.core.http.HttpClientProvider;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
public final class HttpClientProviders {
    private static final String CANNOT_FIND_HTTP_CLIENT =
            "Cannot find any HttpClient provider on the classpath - unable to create a default HttpClient instance";
    private static HttpClientProvider defaultProvider;

    static {
        ServiceLoader<HttpClientProvider> serviceLoader = ServiceLoader.load(HttpClientProvider.class);

        final Iterator<HttpClientProvider> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            defaultProvider = iterator.next();
        }
    }

    private HttpClientProviders() {
        // no-op
    }

    public static HttpClient createInstance() {
        if (defaultProvider != null) {
            return defaultProvider.createInstance();
        } else {
            throw new IllegalStateException(CANNOT_FIND_HTTP_CLIENT);
        }
    }
}
