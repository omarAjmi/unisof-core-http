package com.unisoft.core.http;

import com.unisoft.core.util.log.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * The outgoing Http request. It provides ways to construct {@link HttpRequest} with {@link HttpMethod},
 * {@link URL}, {@link HttpHeader} and request body.
 *
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
public class HttpRequest {

    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private HttpMethod httpMethod;
    private URL url;
    private HttpHeaders headers;
    private Flux<ByteBuffer> body;

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod the HTTP request method
     * @param url        the target address to send the request to
     */
    public HttpRequest(HttpMethod httpMethod, URL url) {
        this.httpMethod = httpMethod;
        this.url = url;
        this.headers = new HttpHeaders();
    }

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod the HTTP request method
     * @param url        the target address to send the request to
     * @throws IllegalArgumentException if {@code url} is null or it cannot be parsed into a valid URL.
     */
    public HttpRequest(HttpMethod httpMethod, String url) {
        this.httpMethod = httpMethod;
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            LogUtil.logExceptionAsWarn(log, () -> {
                throw new IllegalArgumentException("'url' must be a valid URL", e);
            });
        }
        this.headers = new HttpHeaders();
    }

    /**
     * Create a new HttpRequest instance.
     *
     * @param httpMethod the HTTP request method
     * @param url        the target address to send the request to
     * @param headers    the HTTP headers to use with this request
     * @param body       the request content
     */
    public HttpRequest(HttpMethod httpMethod, URL url, HttpHeaders headers, Flux<ByteBuffer> body) {
        this.httpMethod = httpMethod;
        this.url = url;
        this.headers = headers;
        this.body = body;
    }

    /**
     * Get the request method.
     *
     * @return the request method
     */
    public HttpMethod getHttpMethod() {
        return this.httpMethod;
    }

    /**
     * Set the request method.
     *
     * @param httpMethod the request method
     * @return this HttpRequest
     */
    public HttpRequest setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * Get the target address.
     *
     * @return the target address
     */
    public URL getUrl() {
        return this.url;
    }

    /**
     * Set the target address to send the request to.
     *
     * @param url target address as {@link URL}
     * @return this HttpRequest
     */
    public HttpRequest setUrl(URL url) {
        this.url = url;
        return this;
    }

    /**
     * Set the target address to send the request to.
     *
     * @param url target address as a String
     * @return this HttpRequest
     * @throws IllegalArgumentException if {@code url} is null or it cannot be parsed into a valid URL.
     */
    public HttpRequest setUrl(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            LogUtil.logExceptionAsWarn(log, () -> {
                throw new IllegalArgumentException("'url' must be a valid URL", e);
            });
        }
        return this;
    }

    /**
     * Get the request headers.
     *
     * @return headers to be sent
     */
    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Set the request headers.
     *
     * @param headers the set of headers
     * @return this HttpRequest
     */
    public HttpRequest setHeaders(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Set a request header, replacing any existing value.
     * A null for {@code value} will remove the header if one with matching name exists.
     *
     * @param name  the header name
     * @param value the header value
     * @return this HttpRequest
     */
    public HttpRequest setHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    /**
     * Get the request content.
     *
     * @return the content to be send
     */
    public Flux<ByteBuffer> getBody() {
        return this.body;
    }

    /**
     * Set the request content.
     *
     * @param body the request content
     * @return this HttpRequest
     */
    public HttpRequest setBody(String body) {
        final byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        return this.setBody(bodyBytes);
    }

    /**
     * Set the request content.
     * The Content-Length header will be set based on the given content's length
     *
     * @param body the request content
     * @return this HttpRequest
     */
    public HttpRequest setBody(byte[] body) {
        headers.put("Content-Length", String.valueOf(body.length));
        return setBody(Flux.defer(() -> Flux.just(ByteBuffer.wrap(body))));
    }

    /**
     * Set request content.
     * <p>
     * Caller must set the Content-Length header to indicate the length of the content,
     * or use Transfer-Encoding: chunked.
     *
     * @param body the request content
     * @return this HttpRequest
     */
    public HttpRequest setBody(Flux<ByteBuffer> body) {
        this.body = body;
        return this;
    }

    /**
     * Creates a copy of the request.
     * <p>
     * The main purpose of this is so that this HttpRequest can be changed and the resulting
     * HttpRequest can be a backup. This means that the cloned HttpHeaders and body must
     * not be able to change from side effects of this HttpRequest.
     *
     * @return a new HTTP request instance with cloned instances of all mutable properties.
     */
    public HttpRequest copy() {
        final HttpHeaders bufferedHeaders = new HttpHeaders(this.headers);
        return new HttpRequest(this.httpMethod, this.url, bufferedHeaders, this.body);
    }
}
