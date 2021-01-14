package com.unisoft.core.http.serialize;

import com.unisoft.core.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
public enum SerializerEncoding {
    /**
     * JavaScript Object Notation.
     */
    JSON,

    /**
     * Extensible Markup Language.
     */
    XML;


    private static final Logger log = LoggerFactory.getLogger(SerializerEncoding.class);

    private static final String CONTENT_TYPE = "Content-Type";
    private static final Map<String, SerializerEncoding> SUPPORTED_MIME_TYPES;
    private static final TreeMap<String, SerializerEncoding> SUPPORTED_SUFFIXES;
    private static final SerializerEncoding DEFAULT_ENCODING = JSON;


    static {
        // Encodings and suffixes from: https://tools.ietf.org/html/rfc6838
        SUPPORTED_MIME_TYPES = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        SUPPORTED_MIME_TYPES.put("text/xml", XML);
        SUPPORTED_MIME_TYPES.put("application/xml", XML);
        SUPPORTED_MIME_TYPES.put("application/json", JSON);

        SUPPORTED_SUFFIXES = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        SUPPORTED_SUFFIXES.put("xml", XML);
        SUPPORTED_SUFFIXES.put("json", JSON);
    }

    /**
     * Determines the serializer encoding to use based on the Content-Type header.
     *
     * @param headers the headers to check the encoding for.
     * @return the serializer encoding to use for the body. {@link #JSON} if there is no Content-Type header or an
     * unrecognized Content-Type encoding is returned.
     */
    public static SerializerEncoding fromHeaders(HttpHeaders headers) {
        final String mimeContentType = headers.getValue(CONTENT_TYPE);
        if (mimeContentType == null || mimeContentType.isEmpty()) {
            log.warn("'{}' not found. Returning default encoding: {}", CONTENT_TYPE, DEFAULT_ENCODING);
            return DEFAULT_ENCODING;
        }

        final String[] parts = mimeContentType.split(";");
        final SerializerEncoding encoding = SUPPORTED_MIME_TYPES.get(parts[0]);
        if (encoding != null) {
            return encoding;
        }

        final String[] mimeTypeParts = parts[0].split("/");
        if (mimeTypeParts.length != 2) {
            log.warn("Content-Type '{}' does not match mime-type formatting 'type'/'subtype'. "
                    + "Returning default: {}", parts[0], DEFAULT_ENCODING);
            return DEFAULT_ENCODING;
        }

        // Check the suffix if it does not match the full types.
        final String subtype = mimeTypeParts[1];
        final int lastIndex = subtype.lastIndexOf("+");
        if (lastIndex == -1) {
            return DEFAULT_ENCODING;
        }

        final String mimeTypeSuffix = subtype.substring(lastIndex + 1);
        final SerializerEncoding serializerEncoding = SUPPORTED_SUFFIXES.get(mimeTypeSuffix);
        if (serializerEncoding != null) {
            return serializerEncoding;
        }

        log.warn("Content-Type '{}' does not match any supported one. Returning default: {}",
                mimeContentType, DEFAULT_ENCODING);

        return DEFAULT_ENCODING;
    }
}
