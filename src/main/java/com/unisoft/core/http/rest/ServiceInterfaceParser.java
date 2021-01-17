package com.unisoft.core.http.rest;

/**
 * @author omar.H.Ajmi
 * @since 16/01/2021
 */

import com.unisoft.core.http.annotation.Host;
import com.unisoft.core.http.annotation.ServiceInterface;
import com.unisoft.core.http.serialize.SerializerAdapter;
import com.unisoft.core.util.CoreUtil;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type responsible for creating individual Service interface method parsers from a Service
 * interface.
 *
 * @author omar.H.Ajmi
 * @since 16/01/2021
 */
class ServiceInterfaceParser {
    private static final Map<Method, ServiceMethodParser> METHOD_PARSERS = new ConcurrentHashMap<>();
    private final String host;
    private final String serviceName;
    private final SerializerAdapter serializer;

    /**
     * Create a ServiceInterfaceParser object with the provided fully qualified interface
     * name.
     *
     * @param serviceInterface The interface that will be parsed.
     * @param serializer       The serializer that will be used to serialize non-String header values and query values.
     */
    ServiceInterfaceParser(Class<?> serviceInterface, SerializerAdapter serializer) {
        this(serviceInterface, serializer, null);
    }

    /**
     * Create a ServiceInterfaceParser object with the provided fully qualified interface
     * name.
     *
     * @param serviceInterface The interface that will be parsed.
     * @param serializer       The serializer that will be used to serialize non-String header values and query values.
     * @param host             The host of URLs that this Service interface targets.
     * @throws MissingRequiredAnnotationException When an expected annotation on the interface is not provided.
     */
    ServiceInterfaceParser(Class<?> serviceInterface, SerializerAdapter serializer, String host) {
        this.serializer = serializer;

        if (!CoreUtil.isNullOrEmpty(host)) {
            this.host = host;
        } else {
            final Host hostAnnotation = serviceInterface.getAnnotation(Host.class);
            if (hostAnnotation != null && !hostAnnotation.value().isEmpty()) {
                this.host = hostAnnotation.value();
            } else {
                throw new MissingRequiredAnnotationException(Host.class, serviceInterface);
            }
        }

        ServiceInterface serviceAnnotation = serviceInterface.getAnnotation(ServiceInterface.class);
        if (serviceAnnotation != null && !serviceAnnotation.name().isEmpty()) {
            serviceName = serviceAnnotation.name();
        } else {
            throw new MissingRequiredAnnotationException(ServiceInterface.class, serviceInterface);
        }
    }

    /**
     * Get the method parser that is associated with the provided serviceMethod. The method parser
     * can be used to get details about the Service REST API call.
     *
     * @param serviceMethod the method to generate a parser for
     * @return the ServiceMethodParser associated with the provided serviceMethod
     */
    ServiceMethodParser getMethodParser(Method serviceMethod) {
        return METHOD_PARSERS.computeIfAbsent(serviceMethod, sm ->
                new ServiceMethodParser(sm, getHost(), serializer));
    }

    /**
     * Get the desired host that the provided Service interface will target with its REST API
     * calls. This value is retrieved from the @Host annotation placed on the Service interface.
     *
     * @return The value of the @Host annotation.
     */
    String getHost() {
        return this.host;
    }

    String getServiceName() {
        return this.serviceName;
    }
}
