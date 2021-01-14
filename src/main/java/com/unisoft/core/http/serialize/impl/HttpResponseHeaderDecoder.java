package com.unisoft.core.http.serialize.impl;

import com.unisoft.core.http.HttpResponse;
import com.unisoft.core.http.exception.HttpResponseException;
import com.unisoft.core.http.serialize.SerializerAdapter;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Decoder to decode header of HTTP response.
 *
 * @author omar.H.Ajmi
 * @since 20/10/2020
 */
final class HttpResponseHeaderDecoder {
    private HttpResponseHeaderDecoder() {
        // no-op
    }

    /**
     * Decode headers of the http response.
     * <p>
     * The decoding happens when caller subscribed to the returned {@code Mono<Object>}, if the response header is not
     * decodable then {@code Mono.empty()} will be returned.
     *
     * @param httpResponse the response containing the headers to be decoded
     * @param serializer   the adapter to use for decoding
     * @param decodeData   the necessary data required to decode a Http response
     * @return publisher that emits decoded response header upon subscription if header is decodable, no emission if the
     * header is not-decodable
     */
    static Mono<Object> decode(HttpResponse httpResponse, SerializerAdapter serializer,
                               HttpResponseDecodeData decodeData) {
        Type headerType = decodeData.getHeadersType();
        if (headerType == null) {
            return Mono.empty();
        } else {
            return Mono.fromCallable(() -> serializer.deserialize(httpResponse.getHeaders(), headerType))
                    .onErrorResume(IOException.class, e -> Mono.error(new HttpResponseException(
                            "HTTP response has malformed headers", httpResponse, e)));
        }
    }
}
