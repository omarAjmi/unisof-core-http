package com.unisoft.core.http.rest;

import com.unisoft.core.http.HttpHeaders;
import com.unisoft.core.http.HttpRequest;
import com.unisoft.core.http.HttpResponse;
import com.unisoft.core.http.serialize.impl.HttpResponseDecoder;
import com.unisoft.core.util.log.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author omar.H.Ajmi
 * @since 16/01/2021
 */
public class ResponseConstructorsCache {
    private static final Logger log = LoggerFactory.getLogger(ResponseConstructorsCache.class);

    private final Map<Class<?>, Constructor<? extends Response<?>>> cache = new ConcurrentHashMap<>();

    /**
     * Identify the suitable constructor for the given response class.
     *
     * @param responseClass the response class
     * @return identified constructor, null if there is no match
     */
    Constructor<? extends Response<?>> get(Class<? extends Response<?>> responseClass) {
        return this.cache.computeIfAbsent(responseClass, this::locateResponseConstructor);
    }

    /**
     * Identify the most specific constructor for the given response class.
     * <p>
     * The most specific constructor is looked up following order:
     * 1. (httpRequest, statusCode, headers, body, decodedHeaders)
     * 2. (httpRequest, statusCode, headers, body)
     * 3. (httpRequest, statusCode, headers)
     * <p>
     * Developer Note: This method logic can be easily replaced with Java.Stream
     * and associated operators but we're using basic sort and loop constructs
     * here as this method is in hot path and Stream route is consuming a fair
     * amount of resources.
     *
     * @param responseClass the response class
     * @return identified constructor, null if there is no match
     */
    @SuppressWarnings("unchecked")
    private Constructor<? extends Response<?>> locateResponseConstructor(Class<?> responseClass) {
        Constructor<?>[] constructors = responseClass.getDeclaredConstructors();
        // Sort constructors in the "descending order" of parameter count.
        Arrays.sort(constructors, Comparator.comparing(Constructor::getParameterCount, (a, b) -> b - a));
        for (Constructor<?> constructor : constructors) {
            final int paramCount = constructor.getParameterCount();
            if (paramCount >= 3 && paramCount <= 5) {
                try {
                    return (Constructor<? extends Response<?>>) constructor;
                } catch (Throwable t) {
                    throw LogUtil.logExceptionAsError(log, new RuntimeException(t));
                }
            }
        }
        return null;
    }

    /**
     * Invoke the constructor this type represents.
     *
     * @param constructor     the constructor type
     * @param decodedResponse the decoded http response
     * @param bodyAsObject    the http response content
     * @return an instance of a {@link Response} implementation
     */
    Mono<Response<?>> invoke(final Constructor<? extends Response<?>> constructor,
                             final HttpResponseDecoder.HttpDecodedResponse decodedResponse,
                             final Object bodyAsObject) {
        final HttpResponse httpResponse = decodedResponse.getSourceResponse();
        final HttpRequest httpRequest = httpResponse.getRequest();
        final int responseStatusCode = httpResponse.getStatusCode();
        final HttpHeaders responseHeaders = httpResponse.getHeaders();

        final int paramCount = constructor.getParameterCount();
        switch (paramCount) {
            case 3:
                try {
                    return Mono.just(constructor.newInstance(httpRequest,
                            responseStatusCode,
                            responseHeaders));
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    throw LogUtil.logExceptionAsError(log, new RuntimeException("Failed to deserialize 3-parameter"
                            + " response. ", e));
                }
            case 4:
                try {
                    return Mono.just(constructor.newInstance(httpRequest,
                            responseStatusCode,
                            responseHeaders,
                            bodyAsObject));
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    throw LogUtil.logExceptionAsError(log, new RuntimeException("Failed to deserialize 4-parameter"
                            + " response. ", e));
                }
            case 5:
                return decodedResponse.getDecodedHeaders()
                        .map((Function<Object, Response<?>>) decodedHeaders -> {
                            try {
                                return constructor.newInstance(httpRequest,
                                        responseStatusCode,
                                        responseHeaders,
                                        decodedHeaders,
                                        bodyAsObject);
                            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                                throw LogUtil.logExceptionAsError(log, new RuntimeException("Failed to deserialize 5-parameter"
                                        + " response with decoded headers. ", e));
                            }
                        })
                        .switchIfEmpty(Mono.defer((Supplier<Mono<Response<?>>>) () -> {
                            try {
                                return Mono.just(constructor.newInstance(httpRequest,
                                        responseStatusCode,
                                        responseHeaders,
                                        null,
                                        bodyAsObject));
                            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                                throw LogUtil.logExceptionAsError(log, new RuntimeException(
                                        "Failed to deserialize 5-parameter response without decoded headers.", e));
                            }
                        }));
            default:
                throw LogUtil.logExceptionAsError(log,
                        new IllegalStateException("Response constructor with expected parameters not found."));
        }
    }
}
