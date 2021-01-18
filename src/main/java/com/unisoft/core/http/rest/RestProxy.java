package com.unisoft.core.http.rest;

import com.unisoft.core.http.*;
import com.unisoft.core.http.annotation.ResumeOperation;
import com.unisoft.core.http.exception.HttpResponseException;
import com.unisoft.core.http.exception.UnexpectedLengthException;
import com.unisoft.core.http.impl.UnexpectedExceptionInformation;
import com.unisoft.core.http.policy.HttpPipelinePolicy;
import com.unisoft.core.http.serialize.JacksonAdapter;
import com.unisoft.core.http.serialize.SerializerAdapter;
import com.unisoft.core.http.serialize.SerializerEncoding;
import com.unisoft.core.http.serialize.impl.HttpResponseDecoder;
import com.unisoft.core.http.serialize.impl.HttpResponseDecoder.HttpDecodedResponse;
import com.unisoft.core.http.util.UrlBuilder;
import com.unisoft.core.util.*;
import com.unisoft.core.util.log.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.unisoft.core.http.serialize.impl.HttpResponseBodyDecoder.isReturnTypeDecodable;

/**
 * Type to create a proxy implementation for an interface describing REST API methods.
 * <p>
 * RestProxy can create proxy implementations for interfaces with methods that return deserialized Java objects as well
 * as asynchronous Single objects that resolve to a deserialized Java object.
 *
 * @author omar.H.Ajmi
 * @since 16/01/2021
 */
public final class RestProxy implements InvocationHandler {
    private static final ByteBuffer VALIDATION_BUFFER = ByteBuffer.allocate(0);
    private static final String BODY_TOO_LARGE = "Request body emitted %d bytes, more than the expected %d bytes.";
    private static final String BODY_TOO_SMALL = "Request body emitted %d bytes, less than the expected %d bytes.";
    private final Logger log = LoggerFactory.getLogger(RestProxy.class);
    private final HttpPipeline httpPipeline;
    private final SerializerAdapter serializer;
    private final ServiceInterfaceParser interfaceParser;
    private final HttpResponseDecoder decoder;

    private final ResponseConstructorsCache responseConstructorsCache;

    /**
     * Create a RestProxy.
     *
     * @param httpPipeline    the HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param serializer      the serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser the parser that contains information about the interface describing REST API methods that
     *                        this RestProxy "implements".
     */
    private RestProxy(HttpPipeline httpPipeline, SerializerAdapter serializer, ServiceInterfaceParser interfaceParser) {
        this.httpPipeline = httpPipeline;
        this.serializer = serializer;
        this.interfaceParser = interfaceParser;
        this.decoder = new HttpResponseDecoder(this.serializer);
        this.responseConstructorsCache = new ResponseConstructorsCache();
    }

    static Flux<ByteBuffer> validateLength(final HttpRequest request) {
        final Flux<ByteBuffer> bbFlux = request.getBody();
        if (bbFlux == null) {
            return Flux.empty();
        }

        final long expectedLength = Long.parseLong(request.getHeaders().getValue("Content-Length"));

        return Flux.defer(() -> {
            final long[] currentTotalLength = new long[1];
            return Flux.concat(bbFlux, Flux.just(VALIDATION_BUFFER)).handle((buffer, sink) -> {
                if (buffer == null) {
                    return;
                }

                if (buffer == VALIDATION_BUFFER) {
                    if (expectedLength != currentTotalLength[0]) {
                        sink.error(new UnexpectedLengthException(String.format(BODY_TOO_SMALL,
                                currentTotalLength[0], expectedLength), currentTotalLength[0], expectedLength));
                    } else {
                        sink.complete();
                    }
                    return;
                }

                currentTotalLength[0] += buffer.remaining();
                if (currentTotalLength[0] > expectedLength) {
                    sink.error(new UnexpectedLengthException(String.format(BODY_TOO_LARGE,
                            currentTotalLength[0], expectedLength), currentTotalLength[0], expectedLength));
                    return;
                }

                sink.next(buffer);
            });
        });
    }

    private static Exception instantiateUnexpectedException(final UnexpectedExceptionInformation exception,
                                                            final HttpResponse httpResponse,
                                                            final byte[] responseContent,
                                                            final Object responseDecodedContent) {
        final int responseStatusCode = httpResponse.getStatusCode();
        final String contentType = httpResponse.getHeaderValue("Content-Type");
        final String bodyRepresentation;
        if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            bodyRepresentation = "(" + httpResponse.getHeaderValue("Content-Length") + "-byte body)";
        } else {
            bodyRepresentation = responseContent == null || responseContent.length == 0
                    ? "(empty body)"
                    : "\"" + new String(responseContent, StandardCharsets.UTF_8) + "\"";
        }

        Exception result;
        try {
            final Constructor<? extends HttpResponseException> exceptionConstructor =
                    exception.getExceptionType().getConstructor(String.class, HttpResponse.class,
                            exception.getExceptionBodyType());
            result = exceptionConstructor.newInstance("Status code " + responseStatusCode + ", " + bodyRepresentation,
                    httpResponse,
                    responseDecodedContent);
        } catch (ReflectiveOperationException e) {
            String message = "Status code " + responseStatusCode + ", but an instance of "
                    + exception.getExceptionType().getCanonicalName() + " cannot be created."
                    + " Response body: " + bodyRepresentation;

            result = new IOException(message, e);
        }
        return result;
    }

    /**
     * Create an instance of the default serializer.
     *
     * @return the default serializer
     */
    private static SerializerAdapter createDefaultSerializer() {
        return JacksonAdapter.createDefaultSerializerAdapter();
    }

    /**
     * Create the default HttpPipeline.
     *
     * @return the default HttpPipeline
     */
    private static HttpPipeline createDefaultPipeline() {
        return createDefaultPipeline(null);
    }

    /**
     * Create the default HttpPipeline.
     *
     * @param credentialsPolicy the credentials policy factory to use to apply authentication to the pipeline
     * @return the default HttpPipeline
     */
    private static HttpPipeline createDefaultPipeline(HttpPipelinePolicy credentialsPolicy) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        //TODO check default pipeline policies
        /*policies.add(new UserAgentPolicy());
        policies.add(new RetryPolicy());
        policies.add(new CookiePolicy());*/
        if (credentialsPolicy != null) {
            policies.add(credentialsPolicy);
        }

        return new HttpPipelineBuilder()
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .build();
    }

    /**
     * Create a proxy implementation of the provided Service interface.
     *
     * @param serviceInterface the Service interface to provide a proxy implementation for
     * @param <A>              the type of the Service interface
     * @return a proxy implementation of the provided Service interface
     */
    public static <A> A create(Class<A> serviceInterface) {
        return create(serviceInterface, createDefaultPipeline(), createDefaultSerializer());
    }

    /**
     * Create a proxy implementation of the provided Service interface.
     *
     * @param serviceInterface the Service interface to provide a proxy implementation for
     * @param httpPipeline     the HttpPipelinePolicy and HttpClient pipeline that will be used to send Http requests
     * @param <A>              the type of the Service interface
     * @return a proxy implementation of the provided Service interface
     */
    public static <A> A create(Class<A> serviceInterface, HttpPipeline httpPipeline) {
        return create(serviceInterface, httpPipeline, createDefaultSerializer());
    }

    /**
     * Create a proxy implementation of the provided Service interface.
     *
     * @param serviceInterface the Service interface to provide a proxy implementation for
     * @param httpPipeline     the HttpPipelinePolicy and HttpClient pipeline that will be used to send Http requests
     * @param serializer       the serializer that will be used to convert POJOs to and from request and response bodies
     * @param <A>              the type of the Service interface.
     * @return a proxy implementation of the provided Service interface
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> serviceInterface, HttpPipeline httpPipeline, SerializerAdapter serializer) {
        final ServiceInterfaceParser interfaceParser = new ServiceInterfaceParser(serviceInterface, serializer);
        final RestProxy restProxy = new RestProxy(httpPipeline, serializer, interfaceParser);
        return (A) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface},
                restProxy);
    }

    /**
     * Get the ServiceMethodParser for the provided method. The Method must exist on the Service interface that this
     * RestProxy was created to "implement".
     *
     * @param method the method to get a ServiceMethodParser for
     * @return the ServiceMethodParser for the provided method
     */
    private ServiceMethodParser getMethodParser(Method method) {
        return interfaceParser.getMethodParser(method);
    }

    /**
     * Send the provided request asynchronously, applying any request policies provided to the HttpClient instance.
     *
     * @param request     the HTTP request to send
     * @param contextData the context
     * @return a {@link Mono} that emits HttpResponse asynchronously
     */
    public Mono<HttpResponse> send(HttpRequest request, Context contextData) {
        return httpPipeline.send(request, contextData);
    }

    @Override
    public Object invoke(Object proxy, final Method method, Object[] args) {
        try {
            if (method.isAnnotationPresent(ResumeOperation.class)) {
                throw LogUtil.logExceptionAsError(log, Exceptions.propagate(
                        new Exception("The resume operation isn't supported.")));
            }

            final ServiceMethodParser methodParser = getMethodParser(method);
            final HttpRequest request = createHttpRequest(methodParser, args);
            Context context = methodParser.setContext(args)
                    .addData("caller-method", methodParser.getFullyQualifiedMethodName())
                    .addData("unisoft-eagerly-read-response", isReturnTypeDecodable(methodParser.getReturnType()));
            //TODO tracing?

            if (request.getBody() != null) {
                request.setBody(validateLength(request));
            }

            final Mono<HttpResponse> asyncResponse = send(request, context);

            Mono<HttpDecodedResponse> asyncDecodedResponse = this.decoder.decode(asyncResponse, methodParser);

            return handleRestReturnType(asyncDecodedResponse, methodParser, methodParser.getReturnType(), context);
        } catch (IOException e) {
            throw LogUtil.logExceptionAsError(log, Exceptions.propagate(e));
        }
    }

    /**
     * Create a HttpRequest for the provided Service method using the provided arguments.
     *
     * @param methodParser the Service method parser to use
     * @param args         the arguments to use to populate the method's annotation values
     * @return a HttpRequest
     * @throws IOException thrown if the body contents cannot be serialized
     */
    private HttpRequest createHttpRequest(ServiceMethodParser methodParser, Object[] args) throws IOException {
        // Sometimes people pass in a full URL for the value of their PathParam annotated argument.
        // This definitely happens in paging scenarios. In that case, just use the full URL and
        // ignore the Host annotation.
        final String path = methodParser.setPath(args);
        final UrlBuilder pathUrlBuilder = UrlBuilder.parse(path);

        final UrlBuilder urlBuilder;
        if (pathUrlBuilder.getScheme() != null) {
            urlBuilder = pathUrlBuilder;
        } else {
            urlBuilder = new UrlBuilder();

            methodParser.setSchemeAndHost(args, urlBuilder);

            // Set the path after host, concatenating the path
            // segment in the host.
            if (path != null && !path.isEmpty() && !"/".equals(path)) {
                String hostPath = urlBuilder.getPath();
                if (hostPath == null || hostPath.isEmpty() || "/".equals(hostPath) || path.contains("://")) {
                    urlBuilder.setPath(path);
                } else {
                    urlBuilder.setPath(hostPath + "/" + path);
                }
            }
        }

        methodParser.setEncodedQueryParameters(args, urlBuilder);

        final URL url = urlBuilder.toUrl();
        final HttpRequest request = configRequest(new HttpRequest(methodParser.getHttpMethod(), url),
                methodParser, args);

        // Headers from Service method arguments always take precedence over inferred headers from body types
        HttpHeaders httpHeaders = request.getHeaders();
        methodParser.setHeaders(args, httpHeaders);

        return request;
    }

    @SuppressWarnings("unchecked")
    private HttpRequest configRequest(final HttpRequest request, final ServiceMethodParser methodParser,
                                      final Object[] args) throws IOException {
        final Object bodyContentObject = methodParser.setBody(args);
        if (bodyContentObject == null) {
            request.getHeaders().put("Content-Length", "0");
        } else {
            // We read the content type from the @BodyParam annotation
            String contentType = methodParser.getBodyContentType();

            // If this is null or empty, the service interface definition is incomplete and should
            // be fixed to ensure correct definitions are applied
            if (contentType == null || contentType.isEmpty()) {
                if (bodyContentObject instanceof byte[] || bodyContentObject instanceof String) {
                    contentType = ContentType.APPLICATION_OCTET_STREAM;
                } else {
                    contentType = ContentType.APPLICATION_JSON;
                }
//                throw LogUtil.logExceptionAsError(new IllegalStateException(
//                    "The method " + methodParser.getFullyQualifiedMethodName() + " does does not have its content "
//                        + "type correctly specified in its service interface"));
            }

            request.getHeaders().put("Content-Type", contentType);

            // TODO(jogiles) this feels hacky
            boolean isJson = false;
            final String[] contentTypeParts = contentType.split(";");
            for (final String contentTypePart : contentTypeParts) {
                if (contentTypePart.trim().equalsIgnoreCase(ContentType.APPLICATION_JSON)) {
                    isJson = true;
                    break;
                }
            }

            if (isJson) {
                ByteArrayOutputStream stream = new AccessibleByteArrayOutputStream();
                serializer.serialize(bodyContentObject, SerializerEncoding.JSON, stream);

                request.setHeader("Content-Length", String.valueOf(stream.size()));
                request.setBody(Flux.defer(() -> Flux.just(ByteBuffer.wrap(stream.toByteArray(), 0, stream.size()))));
            } else if (FluxUtil.isFluxByteBuffer(methodParser.getBodyJavaType())) {
                // Content-Length or Transfer-Encoding: chunked must be provided by a user-specified header when a
                // Flowable<byte[]> is given for the body.
                request.setBody((Flux<ByteBuffer>) bodyContentObject);
            } else if (bodyContentObject instanceof byte[]) {
                request.setBody((byte[]) bodyContentObject);
            } else if (bodyContentObject instanceof String) {
                final String bodyContentString = (String) bodyContentObject;
                if (!bodyContentString.isEmpty()) {
                    request.setBody(bodyContentString);
                }
            } else if (bodyContentObject instanceof ByteBuffer) {
                request.setBody(Flux.just((ByteBuffer) bodyContentObject));
            } else {
                ByteArrayOutputStream stream = new AccessibleByteArrayOutputStream();
                serializer.serialize(bodyContentObject, SerializerEncoding.fromHeaders(request.getHeaders()), stream);

                request.setHeader("Content-Length", String.valueOf(stream.size()));
                request.setBody(Flux.defer(() -> Flux.just(ByteBuffer.wrap(stream.toByteArray(), 0, stream.size()))));
            }
        }

        return request;
    }

    private Mono<HttpDecodedResponse> ensureExpectedStatus(final Mono<HttpDecodedResponse> asyncDecodedResponse,
                                                           final ServiceMethodParser methodParser) {
        return asyncDecodedResponse
                .flatMap(decodedHttpResponse -> ensureExpectedStatus(decodedHttpResponse, methodParser));
    }

    /**
     * Create a publisher that (1) emits error if the provided response {@code decodedResponse} has 'disallowed status
     * code' OR (2) emits provided response if it's status code ia allowed.
     * <p>
     * 'disallowed status code' is one of the status code defined in the provided ServiceMethodParser or is in the int[]
     * of additional allowed status codes.
     *
     * @param decodedResponse The HttpResponse to check.
     * @param methodParser    The method parser that contains information about the service interface method that initiated
     *                        the HTTP request.
     * @return An async-version of the provided decodedResponse.
     */
    private Mono<HttpDecodedResponse> ensureExpectedStatus(final HttpDecodedResponse decodedResponse,
                                                           final ServiceMethodParser methodParser) {
        final int responseStatusCode = decodedResponse.getSourceResponse().getStatusCode();
        final Mono<HttpDecodedResponse> asyncResult;
        if (!methodParser.isExpectedResponseStatusCode(responseStatusCode)) {
            Mono<byte[]> bodyAsBytes = decodedResponse.getSourceResponse().getBodyAsByteArray();

            asyncResult = bodyAsBytes.flatMap((Function<byte[], Mono<HttpDecodedResponse>>) responseContent -> {
                // bodyAsString() emits non-empty string, now look for decoded version of same string
                Mono<Object> decodedErrorBody = decodedResponse.getDecodedBody(responseContent);

                return decodedErrorBody
                        .flatMap((Function<Object, Mono<HttpDecodedResponse>>) responseDecodedErrorObject -> {
                            // decodedBody() emits 'responseDecodedErrorObject' the successfully decoded exception
                            // body object
                            Throwable exception =
                                    instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode),
                                            decodedResponse.getSourceResponse(),
                                            responseContent,
                                            responseDecodedErrorObject);
                            return Mono.error(exception);
                        })
                        .switchIfEmpty(Mono.defer((Supplier<Mono<HttpDecodedResponse>>) () -> {
                            // decodedBody() emits empty, indicate unable to decode 'responseContent',
                            // create exception with un-decodable content string and without exception body object.
                            Throwable exception =
                                    instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode),
                                            decodedResponse.getSourceResponse(),
                                            responseContent,
                                            null);
                            return Mono.error(exception);
                        }));
            }).switchIfEmpty(Mono.defer((Supplier<Mono<HttpDecodedResponse>>) () -> {
                // bodyAsString() emits empty, indicate no body, create exception empty content string no exception
                // body object.
                Throwable exception =
                        instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode),
                                decodedResponse.getSourceResponse(),
                                null,
                                null);
                return Mono.error(exception);
            }));
        } else {
            asyncResult = Mono.just(decodedResponse);
        }
        return asyncResult;
    }

    private Mono<?> handleRestResponseReturnType(final HttpDecodedResponse response,
                                                 final ServiceMethodParser methodParser,
                                                 final Type entityType) {
        if (TypeUtil.isTypeOrSubTypeOf(entityType, Response.class)) {
            final Type bodyType = TypeUtil.getRestResponseBodyType(entityType);

            if (TypeUtil.isTypeOrSubTypeOf(bodyType, Void.class)) {
                return response.getSourceResponse().getBody().ignoreElements()
                        .then(createResponse(response, entityType, null));
            } else {
                return handleBodyReturnType(response, methodParser, bodyType)
                        .flatMap(bodyAsObject -> createResponse(response, entityType, bodyAsObject))
                        .switchIfEmpty(Mono.defer((Supplier<Mono<Response<?>>>) () -> createResponse(response,
                                entityType, null)));
            }
        } else {
            // For now we're just throwing if the Maybe didn't emit a value.
            return handleBodyReturnType(response, methodParser, entityType);
        }
    }

    @SuppressWarnings("unchecked")
    private Mono<Response<?>> createResponse(HttpDecodedResponse response, Type entityType, Object bodyAsObject) {
        // determine the type of response class. If the type is the 'RestResponse' interface, we will use the
        // 'RestResponseBase' class instead.
        Class<? extends Response<?>> cls = (Class<? extends Response<?>>) TypeUtil.getRawClass(entityType);
        if (cls.equals(Response.class)) {
            cls = (Class) ResponseBase.class;
        }

        Constructor<? extends Response<?>> ctr = this.responseConstructorsCache.get(cls);
        if (ctr != null) {
            return this.responseConstructorsCache.invoke(ctr, response, bodyAsObject);
        } else {
            return Mono.error(new RuntimeException("Cannot find suitable constructor for class " + cls));
        }
    }

    private Mono<?> handleBodyReturnType(final HttpDecodedResponse response,
                                         final ServiceMethodParser methodParser, final Type entityType) {
        final int responseStatusCode = response.getSourceResponse().getStatusCode();
        final HttpMethod httpMethod = methodParser.getHttpMethod();
        final Type returnValueWireType = methodParser.getReturnValueWireType();

        final Mono<?> asyncResult;
        if (httpMethod == HttpMethod.HEAD
                && (TypeUtil.isTypeOrSubTypeOf(
                entityType, Boolean.TYPE) || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class))) {
            boolean isSuccess = (responseStatusCode / 100) == 2;
            asyncResult = Mono.just(isSuccess);
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
            // Mono<byte[]>
            Mono<byte[]> responseBodyBytesAsync = response.getSourceResponse().getBodyAsByteArray();
            if (returnValueWireType == Base64Url.class) {
                // Mono<Base64Url>
                responseBodyBytesAsync =
                        responseBodyBytesAsync.map(base64UrlBytes -> new Base64Url(base64UrlBytes).decodedBytes());
            }
            asyncResult = responseBodyBytesAsync;
        } else if (FluxUtil.isFluxByteBuffer(entityType)) {
            // Mono<Flux<ByteBuffer>>
            asyncResult = Mono.just(response.getSourceResponse().getBody());
        } else {
            // Mono<Object> or Mono<Page<T>>
            asyncResult = response.getDecodedBody((byte[]) null);
        }
        return asyncResult;
    }

    /**
     * Handle the provided asynchronous HTTP response and return the deserialized value.
     *
     * @param asyncHttpDecodedResponse the asynchronous HTTP response to the original HTTP request
     * @param methodParser             the ServiceMethodParser that the request originates from
     * @param returnType               the type of value that will be returned
     * @param context                  Additional context that is passed through the Http pipeline during the service call.
     * @return the deserialized result
     */
    private Object handleRestReturnType(final Mono<HttpDecodedResponse> asyncHttpDecodedResponse,
                                        final ServiceMethodParser methodParser,
                                        final Type returnType,
                                        final Context context) {
        final Mono<HttpDecodedResponse> asyncExpectedResponse =
                ensureExpectedStatus(asyncHttpDecodedResponse, methodParser);

        final Object result;
        if (TypeUtil.isTypeOrSubTypeOf(returnType, Mono.class)) {
            final Type monoTypeParam = TypeUtil.getTypeArgument(returnType);
            if (TypeUtil.isTypeOrSubTypeOf(monoTypeParam, Void.class)) {
                // ProxyMethod ReturnType: Mono<Void>
                result = asyncExpectedResponse.then();
            } else {
                // ProxyMethod ReturnType: Mono<? extends RestResponseBase<?, ?>>
                result = asyncExpectedResponse.flatMap(response ->
                        handleRestResponseReturnType(response, methodParser, monoTypeParam));
            }
        } else if (FluxUtil.isFluxByteBuffer(returnType)) {
            // ProxyMethod ReturnType: Flux<ByteBuffer>
            result = asyncExpectedResponse.flatMapMany(ar -> ar.getSourceResponse().getBody());
        } else if (TypeUtil.isTypeOrSubTypeOf(returnType, void.class) || TypeUtil.isTypeOrSubTypeOf(returnType,
                Void.class)) {
            // ProxyMethod ReturnType: Void
            asyncExpectedResponse.block();
            result = null;
        } else {
            // ProxyMethod ReturnType: T where T != async (Mono, Flux) or sync Void
            // Block the deserialization until a value T is received
            result = asyncExpectedResponse
                    .flatMap(httpResponse -> handleRestResponseReturnType(httpResponse, methodParser, returnType))
                    .block();
        }
        return result;
    }
}
