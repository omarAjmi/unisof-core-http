package com.unisoft.core.http.serialize.impl;

import com.unisoft.core.http.HttpMethod;
import com.unisoft.core.http.HttpRequest;
import com.unisoft.core.http.HttpResponse;
import com.unisoft.core.http.MockHttpResponse;
import com.unisoft.core.http.exception.HttpResponseException;
import com.unisoft.core.http.impl.UnexpectedExceptionInformation;
import com.unisoft.core.http.rest.Response;
import com.unisoft.core.http.rest.ResponseBase;
import com.unisoft.core.http.serialize.JacksonAdapter;
import com.unisoft.core.http.serialize.SerializerAdapter;
import com.unisoft.core.util.Base64Url;
import com.unisoft.core.util.DateTimeRfc1123;
import com.unisoft.core.util.UnixTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author omar.H.Ajmi
 * @since 19/10/2020
 */
class HttpResponseBodyDecoderTests {
    private static final HttpRequest GET_REQUEST = new HttpRequest(HttpMethod.GET, "https://localhost");
    private static final HttpRequest HEAD_REQUEST = new HttpRequest(HttpMethod.HEAD, "https://localhost");

    private static Stream<Arguments> invalidHttpResponseSupplier() {
        return Stream.of(
                // Null response.
                Arguments.of((HttpResponse) null),

                // Response without a request.
                Arguments.of(new MockHttpResponse(null, 200)),

                // Response with a request that is missing the HttpMethod.
                Arguments.of(new MockHttpResponse(new HttpRequest(null, "https://example.com"), 200))
        );
    }

    private static Stream<Arguments> errorResponseSupplier() {
        UnexpectedExceptionInformation exceptionInformation = mock(UnexpectedExceptionInformation.class);
        when(exceptionInformation.getExceptionBodyType()).thenAnswer(invocation -> String.class);

        HttpResponseDecodeData noExpectedStatusCodes = mock(HttpResponseDecodeData.class);
        when(noExpectedStatusCodes.getUnexpectedException(anyInt())).thenReturn(exceptionInformation);

        HttpResponseDecodeData expectedStatusCodes = mock(HttpResponseDecodeData.class);
        when(expectedStatusCodes.isExpectedResponseStatusCode(202)).thenReturn(true);
        when(expectedStatusCodes.getUnexpectedException(anyInt())).thenReturn(exceptionInformation);

        HttpResponse emptyResponse = new MockHttpResponse(GET_REQUEST, 300, (Object) null);
        HttpResponse response = new MockHttpResponse(GET_REQUEST, 300, "expected");
        HttpResponse wrongGoodResponse = new MockHttpResponse(GET_REQUEST, 200, "good response");

        return Stream.of(
                Arguments.of(null, emptyResponse, noExpectedStatusCodes, true, null),
                Arguments.of(null, emptyResponse, expectedStatusCodes, true, null),
                Arguments.of(null, response, noExpectedStatusCodes, false, "expected"),
                Arguments.of(null, response, expectedStatusCodes, false, "expected"),
                Arguments.of("\"expected\"", emptyResponse, noExpectedStatusCodes, false, "expected"),
                Arguments.of("\"expected\"", emptyResponse, expectedStatusCodes, false, "expected"),
                Arguments.of("\"not expected\"", response, noExpectedStatusCodes, false, "not expected"),
                Arguments.of("\"not expected\"", response, expectedStatusCodes, false, "not expected"),
                Arguments.of(null, wrongGoodResponse, expectedStatusCodes, false, "good response"),
                Arguments.of("\"bad response\"", wrongGoodResponse, expectedStatusCodes, false, "bad response"),

                // Improperly formatted JSON string causes MalformedValueException.
                Arguments.of("expected", emptyResponse, noExpectedStatusCodes, true, null)
        );
    }

    private static Stream<Arguments> nonDecodableResponseSupplier() {
        // Types that will cause a response to be non decodable.
        HttpResponseDecodeData nullReturnType = mock(HttpResponseDecodeData.class);
        when(nullReturnType.getReturnType()).thenReturn(null);
        when(nullReturnType.isExpectedResponseStatusCode(200)).thenReturn(true);

        ParameterizedType fluxByteBuffer = mockParameterizedType(Flux.class, ByteBuffer.class);
        HttpResponseDecodeData fluxByteBufferReturnType = mock(HttpResponseDecodeData.class);
        when(fluxByteBufferReturnType.getReturnType()).thenReturn(fluxByteBuffer);
        when(fluxByteBufferReturnType.isExpectedResponseStatusCode(200)).thenReturn(true);

        ParameterizedType monoByteArray = mockParameterizedType(Mono.class, byte[].class);
        HttpResponseDecodeData monoByteArrayReturnType = mock(HttpResponseDecodeData.class);
        when(monoByteArrayReturnType.getReturnType()).thenReturn(monoByteArray);
        when(monoByteArrayReturnType.isExpectedResponseStatusCode(200)).thenReturn(true);

        ParameterizedType voidTypeResponse = mockParameterizedType(ResponseBase.class, int.class, Void.TYPE);
        HttpResponseDecodeData voidTypeResponseReturnType = mock(HttpResponseDecodeData.class);
        when(voidTypeResponseReturnType.getReturnType()).thenReturn(voidTypeResponse);
        when(voidTypeResponseReturnType.isExpectedResponseStatusCode(200)).thenReturn(true);

        ParameterizedType voidClassResponse = mockParameterizedType(ResponseBase.class, int.class, void.class);
        HttpResponseDecodeData voidClassResponseReturnType = mock(HttpResponseDecodeData.class);
        when(voidClassResponseReturnType.getReturnType()).thenReturn(voidClassResponse);
        when(voidClassResponseReturnType.isExpectedResponseStatusCode(200)).thenReturn(true);

        return Stream.of(
                Arguments.of(nullReturnType),
                Arguments.of(fluxByteBufferReturnType),
                Arguments.of(monoByteArrayReturnType),
                Arguments.of(voidTypeResponseReturnType),
                Arguments.of(voidClassResponseReturnType)
        );
    }

    private static Stream<Arguments> decodableResponseSupplier() {
        HttpResponseDecodeData stringDecodeData = mock(HttpResponseDecodeData.class);
        when(stringDecodeData.getReturnType()).thenReturn(String.class);
        when(stringDecodeData.getReturnValueWireType()).thenReturn(String.class);
        when(stringDecodeData.isExpectedResponseStatusCode(200)).thenReturn(true);
        HttpResponse stringResponse = new MockHttpResponse(GET_REQUEST, 200, "hello");

        HttpResponseDecodeData offsetDateTimeDecodeData = mock(HttpResponseDecodeData.class);
        when(offsetDateTimeDecodeData.getReturnType()).thenReturn(OffsetDateTime.class);
        when(offsetDateTimeDecodeData.getReturnValueWireType()).thenReturn(OffsetDateTime.class);
        when(offsetDateTimeDecodeData.isExpectedResponseStatusCode(200)).thenReturn(true);

        OffsetDateTime offsetDateTimeNow = OffsetDateTime.now(ZoneOffset.UTC);
        HttpResponse offsetDateTimeResponse = new MockHttpResponse(GET_REQUEST, 200, offsetDateTimeNow);

        HttpResponseDecodeData dateTimeRfc1123DecodeData = mock(HttpResponseDecodeData.class);
        when(dateTimeRfc1123DecodeData.getReturnType()).thenReturn(OffsetDateTime.class);
        when(dateTimeRfc1123DecodeData.getReturnValueWireType()).thenReturn(DateTimeRfc1123.class);
        when(dateTimeRfc1123DecodeData.isExpectedResponseStatusCode(200)).thenReturn(true);

        DateTimeRfc1123 dateTimeRfc1123Now = new DateTimeRfc1123(offsetDateTimeNow);
        HttpResponse dateTimeRfc1123Response = new MockHttpResponse(GET_REQUEST, 200, dateTimeRfc1123Now);

        HttpResponseDecodeData unixTimeDecodeData = mock(HttpResponseDecodeData.class);
        when(unixTimeDecodeData.getReturnType()).thenReturn(OffsetDateTime.class);
        when(unixTimeDecodeData.getReturnValueWireType()).thenReturn(UnixTime.class);
        when(unixTimeDecodeData.isExpectedResponseStatusCode(200)).thenReturn(true);
        UnixTime unixTimeNow = new UnixTime(offsetDateTimeNow);
        HttpResponse unixTimeResponse = new MockHttpResponse(GET_REQUEST, 200, unixTimeNow);

        ParameterizedType stringList = mockParameterizedType(List.class, String.class);
        HttpResponseDecodeData stringListDecodeData = mock(HttpResponseDecodeData.class);
        when(stringListDecodeData.getReturnType()).thenReturn(stringList);
        when(stringListDecodeData.getReturnValueWireType()).thenReturn(String.class);
        when(stringListDecodeData.isExpectedResponseStatusCode(200)).thenReturn(true);
        List<String> list = Arrays.asList("hello", "unisoft");
        HttpResponse stringListResponse = new MockHttpResponse(GET_REQUEST, 200, list);

        ParameterizedType mapStringString = mockParameterizedType(Map.class, String.class, String.class);
        HttpResponseDecodeData mapStringStringDecodeData = mock(HttpResponseDecodeData.class);
        when(mapStringStringDecodeData.getReturnType()).thenReturn(mapStringString);
        when(mapStringStringDecodeData.getReturnValueWireType()).thenReturn(String.class);
        when(mapStringStringDecodeData.isExpectedResponseStatusCode(200)).thenReturn(true);
        Map<String, String> map = Collections.singletonMap("hello", "unisoft");
        HttpResponse mapStringStringResponse = new MockHttpResponse(GET_REQUEST, 200, map);

        return Stream.of(
                Arguments.of(stringResponse, stringDecodeData, "hello"),
                Arguments.of(offsetDateTimeResponse, offsetDateTimeDecodeData, offsetDateTimeNow),
                Arguments.of(dateTimeRfc1123Response, dateTimeRfc1123DecodeData,
                        new DateTimeRfc1123(dateTimeRfc1123Now.toString()).getDateTime()),
                Arguments.of(unixTimeResponse, unixTimeDecodeData,
                        new UnixTime(Long.parseLong(unixTimeNow.toString())).getDateTime()),
                Arguments.of(stringListResponse, stringListDecodeData, list),
                Arguments.of(mapStringStringResponse, mapStringStringDecodeData, map)
        );
    }

    private static Stream<Arguments> decodeTypeSupplier() {
        HttpResponse badResponse = new MockHttpResponse(GET_REQUEST, 400);
        HttpResponse headResponse = new MockHttpResponse(HEAD_REQUEST, 200);
        HttpResponse getResponse = new MockHttpResponse(GET_REQUEST, 200);

        HttpResponseDecodeData badResponseData = mock(HttpResponseDecodeData.class);
        when(badResponseData.getUnexpectedException(anyInt()))
                .thenReturn(new UnexpectedExceptionInformation(HttpResponseException.class));
        when(badResponseData.isExpectedResponseStatusCode(400)).thenReturn(false);

        HttpResponseDecodeData nonDecodable = mock(HttpResponseDecodeData.class);
        when(nonDecodable.getReturnType()).thenReturn(void.class);
        when(nonDecodable.isExpectedResponseStatusCode(200)).thenReturn(true);

        HttpResponseDecodeData stringReturn = mock(HttpResponseDecodeData.class);
        when(stringReturn.getReturnType()).thenReturn(String.class);
        when(stringReturn.isExpectedResponseStatusCode(200)).thenReturn(true);

        ParameterizedType monoString = mockParameterizedType(Mono.class, String.class);
        HttpResponseDecodeData monoStringReturn = mock(HttpResponseDecodeData.class);
        when(monoStringReturn.getReturnType()).thenReturn(monoString);
        when(monoStringReturn.isExpectedResponseStatusCode(200)).thenReturn(true);

        ParameterizedType responseString = mockParameterizedType(Response.class, String.class);
        HttpResponseDecodeData responseStringReturn = mock(HttpResponseDecodeData.class);
        when(responseStringReturn.getReturnType()).thenReturn(responseString);
        when(responseStringReturn.isExpectedResponseStatusCode(200)).thenReturn(true);

        HttpResponseDecodeData headDecodeData = mock(HttpResponseDecodeData.class);
        when(headDecodeData.isExpectedResponseStatusCode(200)).thenReturn(true);
        return Stream.of(
                Arguments.of(badResponse, badResponseData, Object.class),
                Arguments.of(headResponse, headDecodeData, null),
                Arguments.of(getResponse, nonDecodable, null),
                Arguments.of(getResponse, stringReturn, String.class),
                Arguments.of(getResponse, monoStringReturn, String.class),
                Arguments.of(getResponse, responseStringReturn, String.class)
        );
    }

    private static ParameterizedType mockParameterizedType(Type rawType, Type... actualTypeArguments) {
        ParameterizedType parameterizedType = mock(ParameterizedType.class);
        when(parameterizedType.getRawType()).thenReturn(rawType);
        when(parameterizedType.getActualTypeArguments()).thenReturn(actualTypeArguments);

        return parameterizedType;
    }

    @BeforeEach
    void prepareForMocking() {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    void clearMocks() {
        Mockito.framework().clearInlineMocks();
    }

    @ParameterizedTest
    @MethodSource("invalidHttpResponseSupplier")
    void invalidHttpResponse(HttpResponse response) {
        assertThrows(NullPointerException.class, () -> HttpResponseBodyDecoder.decode(null, response, null, null));

    }

    @ParameterizedTest
    @MethodSource("errorResponseSupplier")
    void errorResponse(String body, HttpResponse httpResponse, HttpResponseDecodeData decodeData,
                       boolean isEmpty, Object expected) {
        StepVerifier.FirstStep<Object> firstStep =
                StepVerifier.create(HttpResponseBodyDecoder.decode(body, httpResponse, new JacksonAdapter(), decodeData));

        if (isEmpty) {
            firstStep.verifyComplete();
        } else {
            firstStep.assertNext(actual -> assertEquals(expected, actual)).verifyComplete();
        }
    }

    @Test
    void ioExceptionInErrorDeserializationReturnsEmpty() throws IOException {
        JacksonAdapter ioExceptionThrower = mock(JacksonAdapter.class);
        when(ioExceptionThrower.deserialize((InputStream) any(), any(), any())).thenThrow(IOException.class);

        HttpResponseDecodeData noExpectedStatusCodes = mock(HttpResponseDecodeData.class);
        when(noExpectedStatusCodes.getUnexpectedException(anyInt()))
                .thenReturn(new UnexpectedExceptionInformation(HttpResponseException.class));
        HttpResponse response = new MockHttpResponse(GET_REQUEST, 300);

        StepVerifier.create(HttpResponseBodyDecoder.decode(null, response, ioExceptionThrower, noExpectedStatusCodes))
                .verifyComplete();
    }

    @Test
    void headRequestReturnsEmpty() {
        HttpResponseDecodeData decodeData = mock(HttpResponseDecodeData.class);
        when(decodeData.isExpectedResponseStatusCode(200)).thenReturn(true);

        HttpResponse response = new MockHttpResponse(HEAD_REQUEST, 200);
        StepVerifier.create(HttpResponseBodyDecoder.decode(null, response, new JacksonAdapter(), decodeData))
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("nonDecodableResponseSupplier")
    void nonDecodableResponse(HttpResponseDecodeData decodeData) {
        HttpResponse response = new MockHttpResponse(GET_REQUEST, 200);

        StepVerifier.create(HttpResponseBodyDecoder.decode(null, response, new JacksonAdapter(), decodeData))
                .verifyComplete();
    }

    @Test
    void emptyResponseReturnsMonoEmpty() {
        HttpResponse response = new MockHttpResponse(GET_REQUEST, 200, (Object) null);
        HttpResponseDecodeData decodeData = mock(HttpResponseDecodeData.class);
        when(decodeData.getReturnType()).thenReturn(String.class);
        when(decodeData.isExpectedResponseStatusCode(200)).thenReturn(true);

        StepVerifier.create(HttpResponseBodyDecoder.decode(null, response, new JacksonAdapter(), decodeData))
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("decodableResponseSupplier")
    void decodableResponse(HttpResponse response, HttpResponseDecodeData decodeData, Object expected) {
        StepVerifier.create(HttpResponseBodyDecoder.decode(null, response, new JacksonAdapter(), decodeData))
                .assertNext(actual -> assertEquals(expected, actual))
                .verifyComplete();
    }

    @Test
    void decodeListBase64UrlResponse() {
        ParameterizedType parameterizedType = mockParameterizedType(List.class, byte[].class);
        HttpResponseDecodeData decodeData = mock(HttpResponseDecodeData.class);
        when(decodeData.getReturnType()).thenReturn(parameterizedType);
        when(decodeData.getReturnValueWireType()).thenReturn(Base64Url.class);
        when(decodeData.isExpectedResponseStatusCode(200)).thenReturn(true);

        List<Base64Url> base64Urls = Arrays.asList(new Base64Url("base"), new Base64Url("64"));
        HttpResponse response = new MockHttpResponse(GET_REQUEST, 200, base64Urls);

        StepVerifier.create(HttpResponseBodyDecoder.decode(null, response, new JacksonAdapter(), decodeData))
                .assertNext(actual -> {
                    assertTrue(actual instanceof List);
                    @SuppressWarnings("unchecked") List<byte[]> decoded = (List<byte[]>) actual;
                    assertEquals(2, decoded.size());
                    assertArrayEquals(base64Urls.get(0).decodedBytes(), decoded.get(0));
                    assertArrayEquals(base64Urls.get(1).decodedBytes(), decoded.get(1));
                }).verifyComplete();
    }

    @Test
    void malformedBodyReturnsError() {
        HttpResponse response = new MockHttpResponse(GET_REQUEST, 200, (Object) null);

        HttpResponseDecodeData decodeData = mock(HttpResponseDecodeData.class);
        when(decodeData.getReturnType()).thenReturn(String.class);
        when(decodeData.getReturnValueWireType()).thenReturn(String.class);
        when(decodeData.isExpectedResponseStatusCode(200)).thenReturn(true);

        StepVerifier.create(HttpResponseBodyDecoder
                .decode("malformed JSON string", response, new JacksonAdapter(), decodeData))
                .verifyError(HttpResponseException.class);
    }

    @Test
    void ioExceptionReturnsError() throws IOException {
        HttpResponse response = new MockHttpResponse(GET_REQUEST, 200, "valid JSON string");

        HttpResponseDecodeData decodeData = mock(HttpResponseDecodeData.class);
        when(decodeData.getReturnType()).thenReturn(String.class);
        when(decodeData.getReturnValueWireType()).thenReturn(String.class);
        when(decodeData.isExpectedResponseStatusCode(200)).thenReturn(true);

        SerializerAdapter serializer = mock(SerializerAdapter.class);
        when(serializer.deserialize((InputStream) any(), any(), any())).thenThrow(IOException.class);

        StepVerifier.create(HttpResponseBodyDecoder.decode(null, response, serializer, decodeData))
                .verifyError(HttpResponseException.class);
    }

    @ParameterizedTest
    @MethodSource("decodeTypeSupplier")
    void decodeType(HttpResponse response, HttpResponseDecodeData data, Type expected) {
        assertEquals(expected, HttpResponseBodyDecoder.decodedType(response, data));
    }
}
