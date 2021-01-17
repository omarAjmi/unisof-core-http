package com.unisoft.core.http.rest;

import com.unisoft.core.http.annotation.ExpectedResponses;
import com.unisoft.core.http.annotation.Get;
import com.unisoft.core.http.annotation.Host;
import com.unisoft.core.http.annotation.ServiceInterface;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ServiceInterfaceParserTest {

    @Test
    public void hostWithNoHostAnnotation() {
        assertThrows(MissingRequiredAnnotationException.class, () -> new ServiceInterfaceParser(TestInterface1.class, null));
    }

    @Test
    public void hostWithNoServiceNameAnnotation() {
        assertThrows(MissingRequiredAnnotationException.class, () -> new ServiceInterfaceParser(TestInterface2.class, null));
    }

    @Test
    public void hostWithHostAnnotation() {
        final ServiceInterfaceParser interfaceParser = new ServiceInterfaceParser(TestInterface3.class, null);
        assertEquals("https://unisoft.com", interfaceParser.getHost());
        assertEquals("myService", interfaceParser.getServiceName());
    }

    @Test
    public void methodParser() {
        final ServiceInterfaceParser interfaceParser = new ServiceInterfaceParser(TestInterface4.class, null);
        final Method testMethod3 = TestInterface4.class.getDeclaredMethods()[0];
        assertEquals("testMethod4", testMethod3.getName());

        final ServiceMethodParser methodParser = interfaceParser.getMethodParser(testMethod3);
        assertNotNull(methodParser);
        assertEquals("com.unisoft.core.http.rest.ServiceInterfaceParserTest$TestInterface4.testMethod4", methodParser.getFullyQualifiedMethodName());

        final ServiceMethodParser methodDetails2 = interfaceParser.getMethodParser(testMethod3);
        assertSame(methodParser, methodDetails2);
    }

    interface TestInterface1 {
        String testMethod1();
    }

    @Host("https://unisoft.com")
    interface TestInterface2 {
    }

    @Host("https://unisoft.com")
    @ServiceInterface(name = "myService")
    interface TestInterface3 {
    }

    @Host("https://unisoft.com")
    @ServiceInterface(name = "myService")
    interface TestInterface4 {
        @Get("my/url/path")
        @ExpectedResponses({200})
        void testMethod4();
    }
}