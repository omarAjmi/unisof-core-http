package com.unisoft.core.http.rest;

import com.unisoft.core.http.ContentType;
import com.unisoft.core.http.HttpHeader;
import com.unisoft.core.http.HttpHeaders;
import com.unisoft.core.http.HttpMethod;
import com.unisoft.core.http.annotation.*;
import com.unisoft.core.http.exception.HttpResponseException;
import com.unisoft.core.http.exception.ResourceNotFoundException;
import com.unisoft.core.http.util.UrlBuilder;
import com.unisoft.core.util.Base64Url;
import com.unisoft.core.util.Context;
import com.unisoft.core.util.DateTimeRfc1123;
import com.unisoft.core.util.UnixTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Stream;

import static com.unisoft.core.http.ContentType.APPLICATION_X_WWW_FORM_URLENCODED;
import static org.junit.jupiter.api.Assertions.*;

class ServiceMethodParserTest {
    private static Stream<Arguments> httpMethodSupplier() throws NoSuchMethodException {
        Class<OperationMethods> clazz = OperationMethods.class;

        return Stream.of(
                Arguments.of(clazz.getDeclaredMethod("getMethod"), HttpMethod.GET, "test",
                        "com.unisoft.core.http.rest.ServiceMethodParserTest$OperationMethods.getMethod"),
                Arguments.of(clazz.getDeclaredMethod("putMethod"), HttpMethod.PUT, "test",
                        "com.unisoft.core.http.rest.ServiceMethodParserTest$OperationMethods.putMethod"),
                Arguments.of(clazz.getDeclaredMethod("headMethod"), HttpMethod.HEAD, "test",
                        "com.unisoft.core.http.rest.ServiceMethodParserTest$OperationMethods.headMethod"),
                Arguments.of(clazz.getDeclaredMethod("deleteMethod"), HttpMethod.DELETE, "test",
                        "com.unisoft.core.http.rest.ServiceMethodParserTest$OperationMethods.deleteMethod"),
                Arguments.of(clazz.getDeclaredMethod("postMethod"), HttpMethod.POST, "test",
                        "com.unisoft.core.http.rest.ServiceMethodParserTest$OperationMethods.postMethod"),
                Arguments.of(clazz.getDeclaredMethod("patchMethod"), HttpMethod.PATCH, "test",
                        "com.unisoft.core.http.rest.ServiceMethodParserTest$OperationMethods.patchMethod")
        );
    }

    private static Stream<Arguments> wireTypesSupplier() throws NoSuchMethodException {
        Class<WireTypesMethods> clazz = WireTypesMethods.class;

        return Stream.of(
                Arguments.of(clazz.getDeclaredMethod("noWireType"), null),
                Arguments.of(clazz.getDeclaredMethod("base64Url"), Base64Url.class),
                Arguments.of(clazz.getDeclaredMethod("unixTime"), UnixTime.class),
                Arguments.of(clazz.getDeclaredMethod("dateTimeRfc1123"), DateTimeRfc1123.class),
                Arguments.of(clazz.getDeclaredMethod("unknownType"), null)
        );
    }

    private static Stream<Arguments> headersSupplier() throws NoSuchMethodException {
        Class<HeaderMethods> clazz = HeaderMethods.class;
        return Stream.of(
                Arguments.of(clazz.getDeclaredMethod("noHeaders"), new HttpHeaders()),
                Arguments.of(clazz.getDeclaredMethod("malformedHeaders"), new HttpHeaders()),
                Arguments.of(clazz.getDeclaredMethod("headers"), new HttpHeaders()
                        .put("name1", "value1").put("name2", "value2").put("name3", "value3")),
                Arguments.of(clazz.getDeclaredMethod("sameKeyTwiceLastWins"), new HttpHeaders().put("name", "value2"))
        );
    }

    private static Stream<Arguments> hostSubstitutionSupplier() throws NoSuchMethodException {
        String sub1RawHost = "https://{sub1}.host.com";
        String sub2RawHost = "https://{sub2}.host.com";

        Class<HostSubstitutionMethods> clazz = HostSubstitutionMethods.class;
        Method noSubstitutions = clazz.getDeclaredMethod("noSubstitutions", String.class);
        Method substitution = clazz.getDeclaredMethod("substitution", String.class);
        Method encodingSubstitution = clazz.getDeclaredMethod("encodingSubstitution", String.class);

        return Stream.of(
                Arguments.of(noSubstitutions, sub1RawHost, toObjectArray("raw"), "https://{sub1}.host.com"),
                Arguments.of(noSubstitutions, sub2RawHost, toObjectArray("raw"), "https://{sub2}.host.com"),
                Arguments.of(substitution, sub1RawHost, toObjectArray("raw"), "https://raw.host.com"),
                Arguments.of(substitution, sub1RawHost, toObjectArray("{sub1}"), "https://{sub1}.host.com"),
                Arguments.of(substitution, sub1RawHost, toObjectArray((String) null), "https://.host.com"),
                Arguments.of(substitution, sub1RawHost, null, "https://{sub1}.host.com"),
                Arguments.of(substitution, sub2RawHost, toObjectArray("raw"), "https://{sub2}.host.com"),
                Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray("raw"), "https://raw.host.com"),
                Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray("{sub1}"), "https://%7Bsub1%7D.host.com"),
                Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray((String) null), "https://.host.com"),
                Arguments.of(substitution, sub1RawHost, null, "https://{sub1}.host.com"),
                Arguments.of(encodingSubstitution, sub2RawHost, toObjectArray("raw"), "https://{sub2}.host.com")
        );
    }

    private static Stream<Arguments> schemeSubstitutionSupplier() throws NoSuchMethodException {
        String sub1RawHost = "{sub1}://raw.host.com";
        String sub2RawHost = "{sub2}://raw.host.com";

        Class<HostSubstitutionMethods> clazz = HostSubstitutionMethods.class;
        Method noSubstitutions = clazz.getDeclaredMethod("noSubstitutions", String.class);
        Method substitution = clazz.getDeclaredMethod("substitution", String.class);
        Method encodingSubstitution = clazz.getDeclaredMethod("encodingSubstitution", String.class);

        return Stream.of(
                Arguments.of(noSubstitutions, sub1RawHost, toObjectArray("raw"), "raw.host.com"),
                Arguments.of(noSubstitutions, sub2RawHost, toObjectArray("raw"), "raw.host.com"),
                Arguments.of(substitution, sub1RawHost, toObjectArray("http"), "http://raw.host.com"),
                Arguments.of(substitution, sub1RawHost, toObjectArray("ĥttps"), "ĥttps://raw.host.com"),
                Arguments.of(substitution, sub1RawHost, toObjectArray((String) null), "raw.host.com"),
                Arguments.of(substitution, sub1RawHost, null, "raw.host.com"),
                Arguments.of(substitution, sub2RawHost, toObjectArray("raw"), "raw.host.com"),
                Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray("http"), "http://raw.host.com"),
                Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray("ĥttps"), "raw.host.com"),
                Arguments.of(encodingSubstitution, sub1RawHost, toObjectArray((String) null), "raw.host.com"),
                Arguments.of(substitution, sub1RawHost, null, "raw.host.com"),
                Arguments.of(encodingSubstitution, sub2RawHost, toObjectArray("raw"), "raw.host.com")
        );
    }

    private static Stream<Arguments> pathSubstitutionSupplier() throws NoSuchMethodException {
        Class<PathSubstitutionMethods> clazz = PathSubstitutionMethods.class;
        Method noSubstitutions = clazz.getDeclaredMethod("noSubstitutions", String.class);
        Method substitution = clazz.getDeclaredMethod("substitution", String.class);
        Method encodedSubstitution = clazz.getDeclaredMethod("encodedSubstitution", String.class);

        return Stream.of(
                Arguments.of(noSubstitutions, toObjectArray("path"), "{sub1}"),
                Arguments.of(encodedSubstitution, toObjectArray("path"), "path"),
                Arguments.of(encodedSubstitution, toObjectArray("{sub1}"), "{sub1}"),
                Arguments.of(encodedSubstitution, toObjectArray((String) null), ""),
                Arguments.of(substitution, toObjectArray("path"), "path"),
                Arguments.of(substitution, toObjectArray("{sub1}"), "%7Bsub1%7D"),
                Arguments.of(substitution, toObjectArray((String) null), "")
        );
    }

    private static Stream<Arguments> querySubstitutionSupplier() throws NoSuchMethodException {
        Class<QuerySubstitutionMethods> clazz = QuerySubstitutionMethods.class;
        Method substitution = clazz.getDeclaredMethod("substitutions", String.class, boolean.class);
        Method encodedSubstitution = clazz.getDeclaredMethod("encodedSubstitutions", String.class, boolean.class);

        return Stream.of(
                Arguments.of(substitution, null, "https://raw.host.com"),
                Arguments.of(substitution, toObjectArray("raw", true), "https://raw.host.com?sub1=raw&sub2=true"),
                Arguments.of(substitution, toObjectArray(null, true), "https://raw.host.com?sub2=true"),
                Arguments.of(substitution, toObjectArray("{sub1}", false),
                        "https://raw.host.com?sub1=%7Bsub1%7D&sub2=false"),
                Arguments.of(encodedSubstitution, null, "https://raw.host.com"),
                Arguments.of(encodedSubstitution, toObjectArray("raw", true), "https://raw.host.com?sub1=raw&sub2=true"),
                Arguments.of(encodedSubstitution, toObjectArray(null, true), "https://raw.host.com?sub2=true"),
                Arguments.of(encodedSubstitution, toObjectArray("{sub1}", false),
                        "https://raw.host.com?sub1={sub1}&sub2=false")
        );
    }

    private static Stream<Arguments> headerSubstitutionSupplier() throws NoSuchMethodException {
        Class<HeaderSubstitutionMethods> clazz = HeaderSubstitutionMethods.class;
        Method addHeaders = clazz.getDeclaredMethod("addHeaders", String.class, boolean.class);
        Method overrideHeaders = clazz.getDeclaredMethod("overrideHeaders", String.class, boolean.class);
        Method headerMap = clazz.getDeclaredMethod("headerMap", Map.class);

        Map<String, String> simpleHeaderMap = Collections.singletonMap("key", "value");
        Map<String, String> expectedSimpleHeadersMap = Collections.singletonMap("x-un-meta-key", "value");

        Map<String, String> complexHeaderMap = new HttpHeaders().put("key1", null).put("key2", "value2").toMap();
        Map<String, String> expectedComplexHeaderMap = Collections.singletonMap("x-un-meta-key2", "value2");

        return Stream.of(
                Arguments.of(addHeaders, null, null),
                Arguments.of(addHeaders, toObjectArray("header", true), createExpectedParameters("header", true)),
                Arguments.of(addHeaders, toObjectArray(null, true), createExpectedParameters(null, true)),
                Arguments.of(addHeaders, toObjectArray("{sub1}", false), createExpectedParameters("{sub1}", false)),
                Arguments.of(overrideHeaders, null, createExpectedParameters("sub1", false)),
                Arguments.of(overrideHeaders, toObjectArray(null, true), createExpectedParameters("sub1", true)),
                Arguments.of(overrideHeaders, toObjectArray("header", false), createExpectedParameters("header", false)),
                Arguments.of(overrideHeaders, toObjectArray("{sub1}", true), createExpectedParameters("{sub1}", true)),
                Arguments.of(headerMap, null, null),
                Arguments.of(headerMap, toObjectArray(simpleHeaderMap), expectedSimpleHeadersMap),
                Arguments.of(headerMap, toObjectArray(complexHeaderMap), expectedComplexHeaderMap)
        );
    }

    private static Stream<Arguments> bodySubstitutionSupplier() throws NoSuchMethodException {
        Class<BodySubstitutionMethods> clazz = BodySubstitutionMethods.class;
        Method jsonBody = clazz.getDeclaredMethod("applicationJsonBody", String.class);
        Method formBody = clazz.getDeclaredMethod("formBody", String.class, Integer.class, OffsetDateTime.class,
                List.class);
        Method encodedFormBody = clazz.getDeclaredMethod("encodedFormBody", String.class, Integer.class,
                OffsetDateTime.class, List.class);
        Method encodedFormKey = clazz.getDeclaredMethod("encodedFormKey", String.class);
        Method encodedFormKey2 = clazz.getDeclaredMethod("encodedFormKey2", String.class);

        OffsetDateTime dob = OffsetDateTime.of(1980, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        List<String> favoriteColors = Arrays.asList("blue", "green");
        List<String> badFavoriteColors = Arrays.asList(null, "green");

        return Stream.of(
                Arguments.of(jsonBody, null, ContentType.APPLICATION_JSON, null),
                Arguments.of(jsonBody, toObjectArray("{name:John Doe,age:40,dob:01-01-1980}"), ContentType.APPLICATION_JSON,
                        "{name:John Doe,age:40,dob:01-01-1980}"),
                Arguments.of(formBody, null, APPLICATION_X_WWW_FORM_URLENCODED, null),
                Arguments.of(formBody, toObjectArray("John Doe", null, dob, null), APPLICATION_X_WWW_FORM_URLENCODED,
                        "name=John+Doe&dob=1980-01-01T00%3A00%3A00Z"),
                Arguments.of(formBody, toObjectArray("John Doe", 40, null, favoriteColors),
                        APPLICATION_X_WWW_FORM_URLENCODED, "name=John+Doe&age=40&favoriteColors=blue&favoriteColors=green"),
                Arguments.of(formBody, toObjectArray("John Doe", 40, null, badFavoriteColors),
                        APPLICATION_X_WWW_FORM_URLENCODED, "name=John+Doe&age=40&favoriteColors=green"),
                Arguments.of(encodedFormBody, null, APPLICATION_X_WWW_FORM_URLENCODED, null),
                Arguments.of(encodedFormBody, toObjectArray("John Doe", null, dob, null), APPLICATION_X_WWW_FORM_URLENCODED,
                        "name=John Doe&dob=1980-01-01T00%3A00%3A00Z"),
                Arguments.of(encodedFormBody, toObjectArray("John Doe", 40, null, favoriteColors),
                        APPLICATION_X_WWW_FORM_URLENCODED, "name=John Doe&age=40&favoriteColors=blue&favoriteColors=green"),
                Arguments.of(encodedFormBody, toObjectArray("John Doe", 40, null, badFavoriteColors),
                        APPLICATION_X_WWW_FORM_URLENCODED, "name=John Doe&age=40&favoriteColors=green"),
                Arguments.of(encodedFormKey, toObjectArray("value"), APPLICATION_X_WWW_FORM_URLENCODED,
                        "x%3Aun%3Avalue=value"),
                Arguments.of(encodedFormKey2, toObjectArray("value"), APPLICATION_X_WWW_FORM_URLENCODED,
                        "x%3Aun%3Avalue=value")
        );
    }

    private static Stream<Arguments> setContextSupplier() throws NoSuchMethodException {
        Method method = OperationMethods.class.getDeclaredMethod("getMethod");
        ServiceMethodParser serviceMethodParser = new ServiceMethodParser(method, "https://raw.host.com");

        Context context = new Context("key", "value");

        return Stream.of(
                Arguments.of(serviceMethodParser, null, Context.NONE),
                Arguments.of(serviceMethodParser, toObjectArray(), Context.NONE),
                Arguments.of(serviceMethodParser, toObjectArray("string"), Context.NONE),
                Arguments.of(serviceMethodParser, toObjectArray(context), context)
        );
    }

    private static Stream<Arguments> expectedStatusCodeSupplier() throws NoSuchMethodException {
        Class<ExpectedStatusCodeMethods> clazz = ExpectedStatusCodeMethods.class;

        return Stream.of(
                Arguments.of(clazz.getDeclaredMethod("noExpectedStatusCodes"), 200, null, true),
                Arguments.of(clazz.getDeclaredMethod("noExpectedStatusCodes"), 201, null, true),
                Arguments.of(clazz.getDeclaredMethod("noExpectedStatusCodes"), 400, null, false),
                Arguments.of(clazz.getDeclaredMethod("only200IsExpected"), 200, new int[]{200}, true),
                Arguments.of(clazz.getDeclaredMethod("only200IsExpected"), 201, new int[]{200}, false),
                Arguments.of(clazz.getDeclaredMethod("only200IsExpected"), 400, new int[]{200}, false),
                Arguments.of(clazz.getDeclaredMethod("retryAfterExpected"), 200, new int[]{429, 503}, false),
                Arguments.of(clazz.getDeclaredMethod("retryAfterExpected"), 201, new int[]{429, 503}, false),
                Arguments.of(clazz.getDeclaredMethod("retryAfterExpected"), 400, new int[]{429, 503}, false),
                Arguments.of(clazz.getDeclaredMethod("retryAfterExpected"), 429, new int[]{429, 503}, true),
                Arguments.of(clazz.getDeclaredMethod("retryAfterExpected"), 503, new int[]{429, 503}, true)
        );
    }

    private static Stream<Arguments> unexpectedStatusCodeSupplier() throws NoSuchMethodException {
        Class<UnexpectedStatusCodeMethods> clazz = UnexpectedStatusCodeMethods.class;
        Method noUnexpectedStatusCodes = clazz.getDeclaredMethod("noUnexpectedStatusCodes");
        Method notFoundStatusCode = clazz.getDeclaredMethod("notFoundStatusCode");
        Method customDefault = clazz.getDeclaredMethod("customDefault");

        return Stream.of(
                Arguments.of(noUnexpectedStatusCodes, 500, HttpResponseException.class),
                Arguments.of(noUnexpectedStatusCodes, 400, HttpResponseException.class),
                Arguments.of(noUnexpectedStatusCodes, 404, HttpResponseException.class),
                Arguments.of(notFoundStatusCode, 500, HttpResponseException.class),
                Arguments.of(notFoundStatusCode, 400, ResourceNotFoundException.class),
                Arguments.of(notFoundStatusCode, 404, ResourceNotFoundException.class),
                Arguments.of(customDefault, 400, ResourceNotFoundException.class),
                Arguments.of(customDefault, 404, ResourceNotFoundException.class)
        );
    }

    private static Object[] toObjectArray(Object... objects) {
        return objects;
    }

    private static Map<String, String> createExpectedParameters(String sub1Value, boolean sub2Value) {
        Map<String, String> expectedParameters = new HashMap<>();
        if (sub1Value != null) {
            expectedParameters.put("sub1", sub1Value);
        }

        expectedParameters.put("sub2", String.valueOf(sub2Value));

        return expectedParameters;
    }

    @Test
    public void noHttpMethodAnnotation() throws NoSuchMethodException {
        Method noHttpMethodAnnotation = OperationMethods.class.getDeclaredMethod("noMethod");
        assertThrows(MissingRequiredAnnotationException.class, () ->
                new ServiceMethodParser(noHttpMethodAnnotation, "s://raw.host.com"));
    }

    @ParameterizedTest
    @MethodSource("httpMethodSupplier")
    public void httpMethod(Method method, HttpMethod expectedMethod, String expectedRelativePath,
                           String expectedFullyQualifiedName) {
        ServiceMethodParser serviceMethodParser = new ServiceMethodParser(method, "https://raw.host.com");
        assertEquals(expectedMethod, serviceMethodParser.getHttpMethod());
        assertEquals(expectedRelativePath, serviceMethodParser.setPath(null));
        assertEquals(expectedFullyQualifiedName, serviceMethodParser.getFullyQualifiedMethodName());
    }

    @ParameterizedTest
    @MethodSource("wireTypesSupplier")
    public void wireTypes(Method method, Class<?> expectedWireType) {
        ServiceMethodParser serviceMethodParser = new ServiceMethodParser(method, "https://raw.host.com");
        assertEquals(expectedWireType, serviceMethodParser.getReturnValueWireType());
    }

    @ParameterizedTest
    @MethodSource("headersSupplier")
    public void headers(Method method, HttpHeaders expectedHeaders) {
        ServiceMethodParser serviceMethodParser = new ServiceMethodParser(method, "https://raw.host.com");

        HttpHeaders actual = new HttpHeaders();
        serviceMethodParser.setHeaders(null, actual);

        for (HttpHeader header : actual) {
            assertEquals(expectedHeaders.getValue(header.getName()), header.getValue());
        }
    }

    @ParameterizedTest
    @MethodSource("hostSubstitutionSupplier")
    public void hostSubstitution(Method method, String rawHost, Object[] arguments, String expectedUrl) {
        ServiceMethodParser serviceMethodParser = new ServiceMethodParser(method, rawHost);
        UrlBuilder urlBuilder = new UrlBuilder();
        serviceMethodParser.setSchemeAndHost(arguments, urlBuilder);

        assertEquals(expectedUrl, urlBuilder.toString());
    }

    @ParameterizedTest
    @MethodSource("schemeSubstitutionSupplier")
    public void schemeSubstitution(Method method, String rawHost, Object[] arguments, String expectedUrl) {
        ServiceMethodParser serviceMethodParser = new ServiceMethodParser(method, rawHost);
        UrlBuilder urlBuilder = new UrlBuilder();
        serviceMethodParser.setSchemeAndHost(arguments, urlBuilder);

        assertEquals(expectedUrl, urlBuilder.toString());
    }

    @ParameterizedTest
    @MethodSource("pathSubstitutionSupplier")
    public void pathSubstitution(Method method, Object[] arguments, String expectedPath) {
        ServiceMethodParser serviceMethodParser = new ServiceMethodParser(method, "https://raw.host.com");
        assertEquals(expectedPath, serviceMethodParser.setPath(arguments));
    }

    @ParameterizedTest
    @MethodSource("querySubstitutionSupplier")
    public void querySubstitution(Method method, Object[] arguments, String expectedUrl) {
        ServiceMethodParser serviceMethodParser = new ServiceMethodParser(method, "https://raw.host.com");

        UrlBuilder urlBuilder = UrlBuilder.parse("https://raw.host.com");
        serviceMethodParser.setEncodedQueryParameters(arguments, urlBuilder);

        assertEquals(expectedUrl, urlBuilder.toString());
    }

    @ParameterizedTest
    @MethodSource("headerSubstitutionSupplier")
    public void headerSubstitution(Method method, Object[] arguments, Map<String, String> expectedHeaders) {
        ServiceMethodParser serviceMethodParser = new ServiceMethodParser(method, "https://raw.host.com");

        HttpHeaders actual = new HttpHeaders();
        serviceMethodParser.setHeaders(arguments, actual);

        for (HttpHeader header : actual) {
            assertEquals(expectedHeaders.get(header.getName()), header.getValue());
        }
    }

    @ParameterizedTest
    @MethodSource("bodySubstitutionSupplier")
    public void bodySubstitution(Method method, Object[] arguments, String expectedBodyContentType,
                                 Object expectedBody) {
        ServiceMethodParser serviceMethodParser = new ServiceMethodParser(method, "https://raw.host.com");

        assertEquals(void.class, serviceMethodParser.getReturnType());
        assertEquals(String.class, serviceMethodParser.getBodyJavaType());
        assertEquals(expectedBodyContentType, serviceMethodParser.getBodyContentType());
        assertEquals(expectedBody, serviceMethodParser.setBody(arguments));
    }

    @ParameterizedTest
    @MethodSource("setContextSupplier")
    public void setContext(ServiceMethodParser serviceMethodParser, Object[] arguments, Context expectedContext) {
        assertEquals(expectedContext, serviceMethodParser.setContext(arguments));
    }

    @ParameterizedTest
    @MethodSource("expectedStatusCodeSupplier")
    public void expectedStatusCodeSupplier(Method method, int statusCode, int[] expectedStatusCodes,
                                           boolean matchesExpected) {
        ServiceMethodParser serviceMethodParser = new ServiceMethodParser(method, "https://raw.host.com");

        if (expectedStatusCodes != null) {
            for (int expectedCode : expectedStatusCodes) {
                assertTrue(serviceMethodParser.isExpectedResponseStatusCode(expectedCode));
            }
        }
        assertEquals(matchesExpected, serviceMethodParser.isExpectedResponseStatusCode(statusCode));
    }

    @ParameterizedTest
    @MethodSource("unexpectedStatusCodeSupplier")
    public void unexpectedStatusCode(Method method, int statusCode, Class<?> expectedExceptionType) {
        ServiceMethodParser serviceMethodParser = new ServiceMethodParser(method, "https://raw.host.com");

        assertEquals(expectedExceptionType, serviceMethodParser.getUnexpectedException(statusCode).getExceptionType());
    }

    interface OperationMethods {
        void noMethod();

        @Get("test")
        void getMethod();

        @Put("test")
        void putMethod();

        @Head("test")
        void headMethod();

        @Delete("test")
        void deleteMethod();

        @Post("test")
        void postMethod();

        @Patch("test")
        void patchMethod();
    }

    interface WireTypesMethods {
        @Get("test")
        void noWireType();

        @Get("test")
        @ReturnValueWireType(Base64Url.class)
        void base64Url();

        @Get("test")
        @ReturnValueWireType(UnixTime.class)
        void unixTime();

        @Get("test")
        @ReturnValueWireType(DateTimeRfc1123.class)
        void dateTimeRfc1123();

        @Get("test")
        @ReturnValueWireType(Boolean.class)
        void unknownType();
    }

    interface HeaderMethods {
        @Get("test")
        void noHeaders();

        @Get("test")
        @Headers({"", ":", "nameOnly:", ":valueOnly"})
        void malformedHeaders();

        @Get("test")
        @Headers({"name1:value1", " name2: value2", "name3 :value3 "})
        void headers();

        @Get("test")
        @Headers({"name:value1", "name:value2"})
        void sameKeyTwiceLastWins();
    }

    interface HostSubstitutionMethods {
        @Get("test")
        void noSubstitutions(String sub1);

        @Get("test")
        void substitution(@HostParam("sub1") String sub1);

        @Get("test")
        void encodingSubstitution(@HostParam(value = "sub1", encoded = false) String sub1);
    }

    interface PathSubstitutionMethods {
        @Get("{sub1}")
        void noSubstitutions(String sub1);

        @Get("{sub1}")
        void substitution(@PathParam("sub1") String sub1);

        @Get("{sub1}")
        void encodedSubstitution(@PathParam(value = "sub1", encoded = true) String sub1);
    }

    interface QuerySubstitutionMethods {
        @Get("test")
        void substitutions(@QueryParam("sub1") String sub1, @QueryParam("sub2") boolean sub2);

        @Get("test")
        void encodedSubstitutions(@QueryParam(value = "sub1", encoded = true) String sub1,
                                  @QueryParam(value = "sub2", encoded = true) boolean sub2);

    }

    interface HeaderSubstitutionMethods {
        @Get("test")
        void addHeaders(@HeaderParam("sub1") String sub1, @HeaderParam("sub2") boolean sub2);

        @Get("test")
        @Headers({"sub1:sub1", "sub2:false"})
        void overrideHeaders(@HeaderParam("sub1") String sub1, @HeaderParam("sub2") boolean sub2);

        @Get("test")
        void headerMap(@HeaderParam("x-un-meta-") Map<String, String> headers);
    }

    interface BodySubstitutionMethods {
        @Get("test")
        void applicationJsonBody(@BodyParam(ContentType.APPLICATION_JSON) String jsonBody);

        @Get("test")
        void formBody(@FormParam("name") String name, @FormParam("age") Integer age,
                      @FormParam("dob") OffsetDateTime dob, @FormParam("favoriteColors") List<String> favoriteColors);

        @Get("test")
        void encodedFormBody(@FormParam(value = "name", encoded = true) String name, @FormParam("age") Integer age,
                             @FormParam("dob") OffsetDateTime dob, @FormParam("favoriteColors") List<String> favoriteColors);

        @Get("test")
        void encodedFormKey(@FormParam(value = "x:un:value") String value);

        @Get("test")
        void encodedFormKey2(@FormParam(value = "x:un:value", encoded = true) String value);
    }

    interface ExpectedStatusCodeMethods {
        @Get("test")
        void noExpectedStatusCodes();

        @Get("test")
        @ExpectedResponses({200})
        void only200IsExpected();

        @Get("test")
        @ExpectedResponses({429, 503})
        void retryAfterExpected();
    }

    interface UnexpectedStatusCodeMethods {
        @Get("test")
        void noUnexpectedStatusCodes();

        @Get("test")
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = {400, 404})
        void notFoundStatusCode();

        @Get("test")
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = {400, 404})
        void customDefault();
    }
}