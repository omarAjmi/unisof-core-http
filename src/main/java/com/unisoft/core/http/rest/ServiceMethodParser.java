package com.unisoft.core.http.rest;

import com.unisoft.core.http.ContentType;
import com.unisoft.core.http.HttpHeader;
import com.unisoft.core.http.HttpHeaders;
import com.unisoft.core.http.HttpMethod;
import com.unisoft.core.http.annotation.*;
import com.unisoft.core.http.exception.HttpResponseException;
import com.unisoft.core.http.impl.UnexpectedExceptionInformation;
import com.unisoft.core.http.serialize.JacksonAdapter;
import com.unisoft.core.http.serialize.SerializerAdapter;
import com.unisoft.core.http.serialize.impl.HttpResponseDecodeData;
import com.unisoft.core.http.util.UrlBuilder;
import com.unisoft.core.util.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author omar.H.Ajmi
 * @since 16/01/2021
 */
public class ServiceMethodParser implements HttpResponseDecodeData {
    private static final Pattern PATTERN_COLON_SLASH_SLASH = Pattern.compile("://");

    private final SerializerAdapter serializer;
    private final String rawHost;
    private final String fullyQualifiedMethodName;
    private final HttpMethod httpMethod;
    private final String relativePath;
    private final List<Substitution> hostSubstitutions = new ArrayList<>();
    private final List<Substitution> pathSubstitutions = new ArrayList<>();
    private final List<Substitution> querySubstitutions = new ArrayList<>();
    private final List<Substitution> formSubstitutions = new ArrayList<>();
    private final List<Substitution> headerSubstitutions = new ArrayList<>();
    private final HttpHeaders headers = new HttpHeaders();
    private final Integer bodyContentMethodParameterIndex;
    private final String bodyContentType;
    private final Type bodyJavaType;
    private final BitSet expectedStatusCodes;
    private final Type returnType;
    private final Type returnValueWireType;
    private final UnexpectedResponseExceptionType[] unexpectedResponseExceptionTypes;
    private Map<Integer, UnexpectedExceptionInformation> exceptionMapping;
    private UnexpectedExceptionInformation defaultException;

    /**
     * Create a ServiceMethodParser object using the provided fully qualified method name.
     *
     * @param serviceMethod the Service method to parse.
     * @param rawHost       the raw host value from the @Host annotation. Before this can be used as the host value in an HTTP
     *                      request, it must be processed through the possible host substitutions.
     */
    ServiceMethodParser(Method serviceMethod, String rawHost) {
        this(serviceMethod, rawHost, JacksonAdapter.createDefaultSerializerAdapter());
    }

    ServiceMethodParser(Method serviceMethod, String rawHost, SerializerAdapter serializer) {
        this.serializer = serializer;
        this.rawHost = rawHost;

        final Class<?> serviceInterface = serviceMethod.getDeclaringClass();

        fullyQualifiedMethodName = serviceInterface.getName() + "." + serviceMethod.getName();

        if (serviceMethod.isAnnotationPresent(Get.class)) {
            this.httpMethod = HttpMethod.GET;
            this.relativePath = serviceMethod.getAnnotation(Get.class).value();
        } else if (serviceMethod.isAnnotationPresent(Put.class)) {
            this.httpMethod = HttpMethod.PUT;
            this.relativePath = serviceMethod.getAnnotation(Put.class).value();
        } else if (serviceMethod.isAnnotationPresent(Head.class)) {
            this.httpMethod = HttpMethod.HEAD;
            this.relativePath = serviceMethod.getAnnotation(Head.class).value();
        } else if (serviceMethod.isAnnotationPresent(Delete.class)) {
            this.httpMethod = HttpMethod.DELETE;
            this.relativePath = serviceMethod.getAnnotation(Delete.class).value();
        } else if (serviceMethod.isAnnotationPresent(Post.class)) {
            this.httpMethod = HttpMethod.POST;
            this.relativePath = serviceMethod.getAnnotation(Post.class).value();
        } else if (serviceMethod.isAnnotationPresent(Patch.class)) {
            this.httpMethod = HttpMethod.PATCH;
            this.relativePath = serviceMethod.getAnnotation(Patch.class).value();
        } else {
            throw new MissingRequiredAnnotationException(Arrays.asList(Get.class, Put.class, Head.class,
                    Delete.class, Post.class, Patch.class), serviceMethod);
        }

        returnType = serviceMethod.getGenericReturnType();

        final ReturnValueWireType returnValueWireTypeAnnotation =
                serviceMethod.getAnnotation(ReturnValueWireType.class);
        if (returnValueWireTypeAnnotation != null) {
            Class<?> returnValueWireType = returnValueWireTypeAnnotation.value();
            if (returnValueWireType == Base64Url.class
                    || returnValueWireType == UnixTime.class
                    || returnValueWireType == DateTimeRfc1123.class) {
                this.returnValueWireType = returnValueWireType;
            } else if (TypeUtil.isTypeOrSubTypeOf(returnValueWireType, List.class)) {
                this.returnValueWireType = returnValueWireType.getGenericInterfaces()[0];
            } else {
                this.returnValueWireType = null;
            }
        } else {
            this.returnValueWireType = null;
        }

        if (serviceMethod.isAnnotationPresent(Headers.class)) {
            final Headers headersAnnotation = serviceMethod.getAnnotation(Headers.class);
            final String[] headers = headersAnnotation.value();
            for (final String header : headers) {
                final int colonIndex = header.indexOf(":");
                if (colonIndex >= 0) {
                    final String headerName = header.substring(0, colonIndex).trim();
                    if (!headerName.isEmpty()) {
                        final String headerValue = header.substring(colonIndex + 1).trim();
                        if (!headerValue.isEmpty()) {
                            this.headers.put(headerName, headerValue);
                        }
                    }
                }
            }
        }

        final ExpectedResponses expectedResponses = serviceMethod.getAnnotation(ExpectedResponses.class);
        if (expectedResponses != null && expectedResponses.value().length > 0) {
            expectedStatusCodes = new BitSet();
            for (int code : expectedResponses.value()) {
                expectedStatusCodes.set(code);
            }
        } else {
            expectedStatusCodes = null;
        }

        unexpectedResponseExceptionTypes = serviceMethod.getAnnotationsByType(UnexpectedResponseExceptionType.class);

        Integer bodyContentMethodParameterIndex = null;
        String bodyContentType = null;
        Type bodyJavaType = null;

        final Annotation[][] allParametersAnnotations = serviceMethod.getParameterAnnotations();
        for (int parameterIndex = 0; parameterIndex < allParametersAnnotations.length; ++parameterIndex) {
            final Annotation[] parameterAnnotations = serviceMethod.getParameterAnnotations()[parameterIndex];
            for (final Annotation annotation : parameterAnnotations) {
                final Class<? extends Annotation> annotationType = annotation.annotationType();
                if (annotationType.equals(HostParam.class)) {
                    final HostParam hostParamAnnotation = (HostParam) annotation;
                    hostSubstitutions.add(new Substitution(hostParamAnnotation.value(), parameterIndex,
                            !hostParamAnnotation.encoded()));
                } else if (annotationType.equals(PathParam.class)) {
                    final PathParam pathParamAnnotation = (PathParam) annotation;
                    pathSubstitutions.add(new Substitution(pathParamAnnotation.value(), parameterIndex,
                            !pathParamAnnotation.encoded()));
                } else if (annotationType.equals(QueryParam.class)) {
                    final QueryParam queryParamAnnotation = (QueryParam) annotation;
                    querySubstitutions.add(new Substitution(queryParamAnnotation.value(), parameterIndex,
                            !queryParamAnnotation.encoded()));
                } else if (annotationType.equals(HeaderParam.class)) {
                    final HeaderParam headerParamAnnotation = (HeaderParam) annotation;
                    headerSubstitutions.add(new Substitution(headerParamAnnotation.value(), parameterIndex, false));
                } else if (annotationType.equals(BodyParam.class)) {
                    final BodyParam bodyParamAnnotation = (BodyParam) annotation;
                    bodyContentMethodParameterIndex = parameterIndex;
                    bodyContentType = bodyParamAnnotation.value();
                    bodyJavaType = serviceMethod.getGenericParameterTypes()[parameterIndex];
                } else if (annotationType.equals(FormParam.class)) {
                    final FormParam formParamAnnotation = (FormParam) annotation;
                    formSubstitutions.add(new Substitution(formParamAnnotation.value(), parameterIndex,
                            !formParamAnnotation.encoded()));
                    bodyContentType = ContentType.APPLICATION_X_WWW_FORM_URLENCODED;
                    bodyJavaType = String.class;
                }
            }
        }

        this.bodyContentMethodParameterIndex = bodyContentMethodParameterIndex;
        this.bodyContentType = bodyContentType;
        this.bodyJavaType = bodyJavaType;
    }

    private static String serialize(SerializerAdapter serializer, Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        } else {
            return serializer.serializeRaw(value);
        }
    }

    private static String serializeFormData(SerializerAdapter serializer, String key, Object value,
                                            boolean shouldEncode) {
        if (value == null) {
            return null;
        }

        String encodedKey = UrlEscapers.FORM_ESCAPER.escape(key);
        if (value instanceof List<?>) {
            return ((List<?>) value).stream()
                    .map(element -> serializeAndEncodeFormValue(serializer, element, shouldEncode))
                    .filter(Objects::nonNull)
                    .map(formValue -> encodedKey + "=" + formValue)
                    .collect(Collectors.joining("&"));
        } else {
            return encodedKey + "=" + serializeAndEncodeFormValue(serializer, value, shouldEncode);
        }
    }

    private static String serializeAndEncodeFormValue(SerializerAdapter serializer, Object value,
                                                      boolean shouldEncode) {
        if (value == null) {
            return null;
        }

        String serializedValue = serializer.serializeRaw(value);

        return shouldEncode ? UrlEscapers.FORM_ESCAPER.escape(serializedValue) : serializedValue;
    }

    /**
     * Get the fully qualified method that was called to invoke this HTTP request.
     *
     * @return the fully qualified method that was called to invoke this HTTP request
     */
    public String getFullyQualifiedMethodName() {
        return fullyQualifiedMethodName;
    }

    /**
     * Get the HTTP method that will be used to complete the Service method's request.
     *
     * @return the HTTP method that will be used to complete the Service method's request
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Sets the scheme and host to use for HTTP requests for this Service method.
     *
     * @param serviceMethodArguments The arguments to use for scheme and host substitutions.
     * @param urlBuilder             The {@link UrlBuilder} that will have its scheme and host set.
     */
    public void setSchemeAndHost(Object[] serviceMethodArguments, UrlBuilder urlBuilder) {
        final String substitutedHost = applySubstitutions(rawHost, hostSubstitutions, serviceMethodArguments);
        final String[] substitutedHostParts = PATTERN_COLON_SLASH_SLASH.split(substitutedHost);

        if (substitutedHostParts.length >= 2) {
            urlBuilder.setScheme(substitutedHostParts[0]);
            urlBuilder.setHost(substitutedHostParts[1]);
        } else if (substitutedHostParts.length == 1) {
            urlBuilder.setScheme(substitutedHostParts[0]);
            urlBuilder.setHost(substitutedHost);
        } else {
            urlBuilder.setHost(substitutedHost);
        }
    }

    /**
     * Get the path that will be used to complete the Service method's request.
     *
     * @param methodArguments the method arguments to use with the path substitutions
     * @return the path value with its placeholders replaced by the matching substitutions
     */
    public String setPath(Object[] methodArguments) {
        return applySubstitutions(relativePath, pathSubstitutions, methodArguments);
    }

    /**
     * Sets the encoded query parameters that have been added to this value based on the provided method arguments into
     * the passed {@link UrlBuilder}.
     *
     * @param serviceMethodArguments the arguments that will be used to create the query parameters' values
     * @param urlBuilder             The {@link UrlBuilder} where the encoded query parameters will be set.
     */
    public void setEncodedQueryParameters(Object[] serviceMethodArguments, UrlBuilder urlBuilder) {
        if (serviceMethodArguments == null) {
            return;
        }

        for (Substitution substitution : querySubstitutions) {
            final int parameterIndex = substitution.getMethodParameterIndex();
            if (0 <= parameterIndex && parameterIndex < serviceMethodArguments.length) {
                final Object methodArgument = serviceMethodArguments[substitution.getMethodParameterIndex()];
                String parameterValue = serialize(serializer, methodArgument);
                if (parameterValue != null) {
                    if (substitution.shouldEncode()) {
                        parameterValue = UrlEscapers.QUERY_ESCAPER.escape(parameterValue);
                    }
                    urlBuilder.setQueryParameter(substitution.getUrlParameterName(), parameterValue);
                }
            }
        }
    }

    /**
     * Sets the headers that have been added to this value based on the provided method arguments into the passed
     * {@link HttpHeaders}.
     *
     * @param serviceMethodArguments The arguments that will be used to create the headers' values.
     * @param httpHeaders            The {@link HttpHeaders} where the header values will be set.
     */
    public void setHeaders(Object[] serviceMethodArguments, HttpHeaders httpHeaders) {
        for (HttpHeader header : headers) {
            httpHeaders.put(header.getName(), header.getValue());
        }

        if (serviceMethodArguments == null) {
            return;
        }

        for (Substitution headerSubstitution : headerSubstitutions) {
            final int parameterIndex = headerSubstitution.getMethodParameterIndex();
            if (0 <= parameterIndex && parameterIndex < serviceMethodArguments.length) {
                final Object methodArgument = serviceMethodArguments[headerSubstitution.getMethodParameterIndex()];
                if (methodArgument instanceof Map) {
                    @SuppressWarnings("unchecked") final Map<String, ?> headerCollection =
                            (Map<String, ?>) methodArgument;
                    final String headerCollectionPrefix = headerSubstitution.getUrlParameterName();
                    for (final Map.Entry<String, ?> headerCollectionEntry : headerCollection.entrySet()) {
                        final String headerName = headerCollectionPrefix + headerCollectionEntry.getKey();
                        final String headerValue = serialize(serializer, headerCollectionEntry.getValue());
                        if (headerValue != null) {
                            httpHeaders.put(headerName, headerValue);
                        }
                    }
                } else {
                    final String headerName = headerSubstitution.getUrlParameterName();
                    final String headerValue = serialize(serializer, methodArgument);
                    if (headerValue != null) {
                        httpHeaders.put(headerName, headerValue);
                    }
                }
            }
        }
    }

    /**
     * Get the {@link Context} passed into the proxy method.
     *
     * @param serviceMethodArguments the arguments passed to the proxy method
     * @return the context, or {@link Context#NONE} if no context was provided
     */
    public Context setContext(Object[] serviceMethodArguments) {
        Context context = CoreUtil.findFirstOfType(serviceMethodArguments, Context.class);

        return (context != null) ? context : Context.NONE;
    }

    /**
     * Get whether or not the provided response status code is one of the expected status codes for this Service
     * method.
     * <p>
     * 1. If the returned int[] is null, then all 2XX status codes are considered as success code.
     * 2. If the returned int[] is not-null, only the codes in the array are considered as success code.
     *
     * @param statusCode the status code that was returned in the HTTP response
     * @return whether or not the provided response status code is one of the expected status codes for this Service
     * method
     */
    @Override
    public boolean isExpectedResponseStatusCode(final int statusCode) {
        return expectedStatusCodes == null
                ? statusCode < 400
                : expectedStatusCodes.get(statusCode);
    }

    /**
     * Get the {@link UnexpectedExceptionInformation} that will be used to generate a RestException if the HTTP response
     * status code is not one of the expected status codes.
     * <p>
     * If an UnexpectedExceptionInformation is not found for the status code the default UnexpectedExceptionInformation
     * will be returned.
     *
     * @param code Exception HTTP status code return from a REST API.
     * @return the UnexpectedExceptionInformation to generate an exception to throw or return.
     */
    @Override
    public UnexpectedExceptionInformation getUnexpectedException(int code) {
        if (exceptionMapping == null) {
            exceptionMapping = processUnexpectedResponseExceptionTypes();
        }

        return exceptionMapping.getOrDefault(code, defaultException);
    }

    /**
     * Get the object to be used as the value of the HTTP request.
     *
     * @param serviceMethodArguments the method arguments to get the value object from
     * @return the object that will be used as the body of the HTTP request
     */
    public Object setBody(Object[] serviceMethodArguments) {
        Object result = null;

        if (bodyContentMethodParameterIndex != null
                && serviceMethodArguments != null
                && 0 <= bodyContentMethodParameterIndex
                && bodyContentMethodParameterIndex < serviceMethodArguments.length) {
            result = serviceMethodArguments[bodyContentMethodParameterIndex];
        }

        if (!CoreUtil.isNullOrEmpty(formSubstitutions) && serviceMethodArguments != null) {
            result = formSubstitutions.stream()
                    .map(substitution -> serializeFormData(serializer, substitution.getUrlParameterName(),
                            serviceMethodArguments[substitution.getMethodParameterIndex()], substitution.shouldEncode()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("&"));
        }

        return result;
    }

    /**
     * Get the Content-Type of the body of this Service method.
     *
     * @return the Content-Type of the body of this Service method
     */
    public String getBodyContentType() {
        return bodyContentType;
    }

    /**
     * Get the return type for the method that this object describes.
     *
     * @return the return type for the method that this object describes.
     */
    @Override
    public Type getReturnType() {
        return returnType;
    }

    /**
     * Get the type of the body parameter to this method, if present.
     *
     * @return the return type of the body parameter to this method
     */
    public Type getBodyJavaType() {
        return bodyJavaType;
    }

    /**
     * Get the type that the return value will be send across the network as. If returnValueWireType is not null, then
     * the raw HTTP response body will need to parsed to this type and then converted to the actual returnType.
     *
     * @return the type that the raw HTTP response body will be sent as
     */
    @Override
    public Type getReturnValueWireType() {
        return returnValueWireType;
    }

    private String applySubstitutions(String originalValue, Iterable<Substitution> substitutions,
                                      Object[] methodArguments) {
        String result = originalValue;

        if (methodArguments != null) {
            for (Substitution substitution : substitutions) {
                final int substitutionParameterIndex = substitution.getMethodParameterIndex();
                if (0 <= substitutionParameterIndex && substitutionParameterIndex < methodArguments.length) {
                    final Object methodArgument = methodArguments[substitutionParameterIndex];

                    String substitutionValue = serialize(serializer, methodArgument);
                    if (substitutionValue != null && !substitutionValue.isEmpty() && substitution.shouldEncode()) {
                        substitutionValue = UrlEscapers.PATH_ESCAPER.escape(substitutionValue);
                    }
                    // if a parameter is null, we treat it as empty string. This is
                    // assuming no {...} will be allowed otherwise in a path template
                    if (substitutionValue == null) {
                        substitutionValue = "";
                    }
                    result = result.replace("{" + substitution.getUrlParameterName() + "}", substitutionValue);
                }
            }
        }

        return result;
    }

    private Map<Integer, UnexpectedExceptionInformation> processUnexpectedResponseExceptionTypes() {
        HashMap<Integer, UnexpectedExceptionInformation> exceptionHashMap = new HashMap<>();

        for (UnexpectedResponseExceptionType exceptionAnnotation : unexpectedResponseExceptionTypes) {
            UnexpectedExceptionInformation exception = new UnexpectedExceptionInformation(exceptionAnnotation.value());
            if (exceptionAnnotation.code().length == 0) {
                defaultException = exception;
            } else {
                for (int statusCode : exceptionAnnotation.code()) {
                    exceptionHashMap.put(statusCode, exception);
                }
            }
        }

        if (defaultException == null) {
            defaultException = new UnexpectedExceptionInformation(HttpResponseException.class);
        }

        return exceptionHashMap;
    }
}
