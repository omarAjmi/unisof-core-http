package com.unisoft.core.http.util;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class UrlBuilderTest {

    @Test
    void scheme() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http");
        assertEquals("http://", builder.toString());
    }

    @Test
    void schemeWhenSchemeIsNull() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http");
        builder.setScheme(null);
        assertNull(builder.getScheme());
    }

    @Test
    void schemeWhenSchemeIsEmpty() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http");
        builder.setScheme("");
        assertNull(builder.getScheme());
    }

    @Test
    void schemeWhenSchemeIsNotEmpty() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http");
        builder.setScheme("https");
        assertEquals("https", builder.getScheme());
    }

    @Test
    void schemeWhenSchemeContainsTerminator() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http://");
        assertEquals("http", builder.getScheme());
        assertNull(builder.getHost());
        assertEquals("http://", builder.toString());
    }

    @Test
    void schemeWhenSchemeContainsHost() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http://www.example.com");
        assertEquals("http", builder.getScheme());
        assertEquals("www.example.com", builder.getHost());
        assertEquals("http://www.example.com", builder.toString());
    }

    @Test
    void schemeAndHost() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http")
                .setHost("www.example.com");
        assertEquals("http://www.example.com", builder.toString());
    }

    @Test
    void schemeAndHostWhenHostHasWhitespace() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http")
                .setHost("www.exa mple.com");
        assertEquals("http://www.exa mple.com", builder.toString());
    }

    @Test
    void host() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com");
        assertEquals("www.example.com", builder.toString());
    }

    @Test
    void hostWhenHostIsNull() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com");
        builder.setHost(null);
        assertNull(builder.getHost());
    }

    @Test
    void hostWhenHostIsEmpty() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com");
        builder.setHost("");
        assertNull(builder.getHost());
    }

    @Test
    void hostWhenHostIsNotEmpty() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com");
        builder.setHost("www.bing.com");
        assertEquals("www.bing.com", builder.getHost());
    }

    @Test
    void hostWhenHostContainsSchemeTerminator() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("://www.example.com");
        assertNull(builder.getScheme());
        assertEquals("www.example.com", builder.getHost());
        assertEquals("www.example.com", builder.toString());
    }

    @Test
    void hostWhenHostContainsScheme() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("https://www.example.com");
        assertEquals("https", builder.getScheme());
        assertEquals("www.example.com", builder.getHost());
        assertEquals("https://www.example.com", builder.toString());
    }

    @Test
    void hostWhenHostContainsColonButNoPort() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com:");
        assertEquals("www.example.com", builder.getHost());
        assertNull(builder.getPort());
        assertEquals("www.example.com", builder.toString());
    }

    @Test
    void hostWhenHostContainsPort() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com:1234");
        assertEquals("www.example.com", builder.getHost());
        assertEquals(1234, builder.getPort());
        assertEquals("www.example.com:1234", builder.toString());
    }

    @Test
    void hostWhenHostContainsForwardSlashButNoPath() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com/");
        assertEquals("www.example.com", builder.getHost());
        assertEquals("/", builder.getPath());
        assertEquals("www.example.com/", builder.toString());
    }

    @Test
    void hostWhenHostContainsPath() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com/index.html");
        assertEquals("www.example.com", builder.getHost());
        assertEquals("/index.html", builder.getPath());
        assertEquals("www.example.com/index.html", builder.toString());
    }

    @Test
    void hostWhenHostContainsQuestionMarkButNoQuery() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com?");
        assertEquals("www.example.com", builder.getHost());
        assertEquals(0, builder.getQuery().size());
        assertEquals("www.example.com", builder.toString());
    }

    @Test
    void hostWhenHostContainsQuery() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com?a=b");
        assertEquals("www.example.com", builder.getHost());
        assertThat(builder.toString(), CoreMatchers.containsString("a=b"));
        assertEquals("www.example.com?a=b", builder.toString());
    }

    @Test
    void hostWhenHostHasWhitespace() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.exampl e.com");
        assertEquals("www.exampl e.com", builder.toString());
    }

    @Test
    void hostAndPath() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com")
                .setPath("my/path");
        assertEquals("www.example.com/my/path", builder.toString());
    }

    @Test
    void hostAndPathWithSlashAfterHost() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com/")
                .setPath("my/path");
        assertEquals("www.example.com/my/path", builder.toString());
    }

    @Test
    void hostAndPathWithSlashBeforePath() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com")
                .setPath("/my/path");
        assertEquals("www.example.com/my/path", builder.toString());
    }

    @Test
    void hostAndPathWithSlashAfterHostAndBeforePath() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com/")
                .setPath("/my/path");
        assertEquals("www.example.com/my/path", builder.toString());
    }

    @Test
    void hostAndPathWithWhitespaceInPath() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com")
                .setPath("my path");
        assertEquals("www.example.com/my path", builder.toString());
    }

    @Test
    void hostAndPathWithPlusInPath() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com")
                .setPath("my+path");
        assertEquals("www.example.com/my+path", builder.toString());
    }

    @Test
    void hostAndPathWithPercent20InPath() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com")
                .setPath("my%20path");
        assertEquals("www.example.com/my%20path", builder.toString());
    }

    @Test
    void portInt() {
        final UrlBuilder builder = new UrlBuilder()
                .setPort(50);
        assertEquals(50, builder.getPort());
        assertEquals(":50", builder.toString());
    }

    @Test
    void portStringWithNull() {
        final UrlBuilder builder = new UrlBuilder()
                .setPort(null);
        assertNull(builder.getPort());
        assertEquals("", builder.toString());
    }

    @Test
    void portStringWithEmpty() {
        final UrlBuilder builder = new UrlBuilder()
                .setPort("");
        assertNull(builder.getPort());
        assertEquals("", builder.toString());
    }

    @Test
    void portString() {
        final UrlBuilder builder = new UrlBuilder()
                .setPort("50");
        assertEquals(50, builder.getPort());
        assertEquals(":50", builder.toString());
    }

    @Test
    void portStringWithForwardSlashButNoPath() {
        final UrlBuilder builder = new UrlBuilder()
                .setPort("50/");
        assertEquals(50, builder.getPort());
        assertEquals("/", builder.getPath());
        assertEquals(":50/", builder.toString());
    }

    @Test
    void portStringPath() {
        final UrlBuilder builder = new UrlBuilder()
                .setPort("50/index.html");
        assertEquals(50, builder.getPort());
        assertEquals("/index.html", builder.getPath());
        assertEquals(":50/index.html", builder.toString());
    }

    @Test
    void portStringWithQuestionMarkButNoQuery() {
        final UrlBuilder builder = new UrlBuilder()
                .setPort("50?");
        assertEquals(50, builder.getPort());
        assertEquals(0, builder.getQuery().size());
        assertEquals(":50", builder.toString());
    }

    @Test
    void portStringQuery() {
        final UrlBuilder builder = new UrlBuilder()
                .setPort("50?a=b&c=d");
        assertEquals(50, builder.getPort());
        assertThat(builder.toString(), CoreMatchers.containsString("?a=b&c=d"));
        assertEquals(":50?a=b&c=d", builder.toString());
    }

    @Test
    void portStringWhenPortIsNull() {
        final UrlBuilder builder = new UrlBuilder()
                .setPort(8080);
        builder.setPort(null);
        assertNull(builder.getPort());
    }

    @Test
    void portStringWhenPortIsEmpty() {
        final UrlBuilder builder = new UrlBuilder()
                .setPort(8080);
        builder.setPort("");
        assertNull(builder.getPort());
    }

    @Test
    void portStringWhenPortIsNotEmpty() {
        final UrlBuilder builder = new UrlBuilder()
                .setPort(8080);
        builder.setPort("123");
        assertEquals(123, builder.getPort());
    }

    @Test
    void schemeAndHostAndOneQueryParameter() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http")
                .setHost("www.example.com")
                .setQueryParameter("A", "B");
        assertEquals("http://www.example.com?A=B", builder.toString());
    }

    @Test
    void schemeAndHostAndOneQueryParameterWhenQueryParameterNameHasWhitespace() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http")
                .setHost("www.example.com")
                .setQueryParameter("App les", "B");
        assertEquals("http://www.example.com?App les=B", builder.toString());
    }

    @Test
    void schemeAndHostAndOneQueryParameterWhenQueryParameterNameHasPercent20() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http")
                .setHost("www.example.com")
                .setQueryParameter("App%20les", "B");
        assertEquals("http://www.example.com?App%20les=B", builder.toString());
    }

    @Test
    void schemeAndHostAndOneQueryParameterWhenQueryParameterValueHasWhitespace() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http")
                .setHost("www.example.com")
                .setQueryParameter("Apples", "Go od");
        assertEquals("http://www.example.com?Apples=Go od", builder.toString());
    }

    @Test
    void schemeAndHostAndOneQueryParameterWhenQueryParameterValueHasPercent20() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http")
                .setHost("www.example.com")
                .setQueryParameter("Apples", "Go%20od");
        assertEquals("http://www.example.com?Apples=Go%20od", builder.toString());
    }

    @Test
    void schemeAndHostAndTwoQueryParameters() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http")
                .setHost("www.example.com")
                .setQueryParameter("A", "B")
                .setQueryParameter("C", "D");
        assertEquals("http://www.example.com?A=B&C=D", builder.toString());
    }

    @Test
    void schemeAndHostAndPathAndTwoQueryParameters() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http")
                .setHost("www.example.com")
                .setQueryParameter("A", "B")
                .setQueryParameter("C", "D")
                .setPath("index.html");
        assertEquals("http://www.example.com/index.html?A=B&C=D", builder.toString());
    }

    @Test
    void pathWhenBuilderPathIsNullAndPathIsNull() {
        final UrlBuilder builder = new UrlBuilder();
        builder.setPath(null);
        assertNull(builder.getPath());
    }

    @Test
    void pathWhenBuilderPathIsNullAndPathIsEmptyString() {
        final UrlBuilder builder = new UrlBuilder();
        builder.setPath("");
        assertNull(builder.getPath());
    }

    @Test
    void pathWhenBuilderPathIsNullAndPathIsForwardSlash() {
        final UrlBuilder builder = new UrlBuilder();
        builder.setPath("/");
        assertEquals("/", builder.getPath());
    }

    @Test
    void pathWhenBuilderPathIsNullAndPath() {
        final UrlBuilder builder = new UrlBuilder();
        builder.setPath("test/path.html");
        assertEquals("test/path.html", builder.getPath());
    }

    @Test
    void pathWhenBuilderPathIsForwardSlashAndPathIsNull() {
        final UrlBuilder builder = new UrlBuilder()
                .setPath("/");
        builder.setPath(null);
        assertNull(builder.getPath());
    }

    @Test
    void pathWhenBuilderPathIsForwardSlashAndPathIsEmptyString() {
        final UrlBuilder builder = new UrlBuilder()
                .setPath("/");
        builder.setPath("");
        assertNull(builder.getPath());
    }

    @Test
    void pathWhenBuilderPathIsForwardSlashAndPathIsForwardSlash() {
        final UrlBuilder builder = new UrlBuilder()
                .setPath("/");
        builder.setPath("/");
        assertEquals("/", builder.getPath());
    }

    @Test
    void pathWhenBuilderPathIsForwardSlashAndPath() {
        final UrlBuilder builder = new UrlBuilder()
                .setPath("/");
        builder.setPath("test/path.html");
        assertEquals("test/path.html", builder.getPath());
    }

    @Test
    void pathWhenHostContainsPath() {
        final UrlBuilder builder = new UrlBuilder()
                .setHost("www.example.com/site")
                .setPath("index.html");
        assertEquals("www.example.com", builder.getHost());
        assertEquals("index.html", builder.getPath());
        assertEquals("www.example.com/index.html", builder.toString());
    }

    @Test
    void pathFirstWhenHostContainsPath() {
        final UrlBuilder builder = new UrlBuilder()
                .setPath("index.html")
                .setHost("www.example.com/site");
        assertEquals("www.example.com", builder.getHost());
        assertEquals("/site", builder.getPath());
        assertEquals("www.example.com/site", builder.toString());
    }

    @Test
    void emptyPathWhenHostContainsPath() {
        final UrlBuilder builder = new UrlBuilder()
                .setPath("")
                .setHost("www.example.com/site");
        assertEquals("www.example.com", builder.getHost());
        assertEquals("/site", builder.getPath());
        assertEquals("www.example.com/site", builder.toString());
    }

    @Test
    void slashPathWhenHostContainsPath() {
        final UrlBuilder builder = new UrlBuilder()
                .setPath("//")
                .setHost("www.example.com/site");
        assertEquals("www.example.com", builder.getHost());
        assertEquals("/site", builder.getPath());
        assertEquals("www.example.com/site", builder.toString());
    }

    @Test
    void withAbsolutePath() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http")
                .setHost("www.example.com")
                .setPath("http://www.othersite.com");
        assertEquals("http://www.othersite.com", builder.toString());
    }

    @Test
    void queryInPath() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http")
                .setHost("www.example.com")
                .setPath("mypath?thing=stuff")
                .setQueryParameter("otherthing", "otherstuff");
        assertEquals("http://www.example.com/mypath?thing=stuff&otherthing=otherstuff", builder.toString());
    }

    @Test
    void withAbsolutePathAndQuery() {
        final UrlBuilder builder = new UrlBuilder()
                .setScheme("http")
                .setHost("www.example.com")
                .setPath("http://www.othersite.com/mypath?thing=stuff")
                .setQueryParameter("otherthing", "otherstuff");
        assertEquals("http://www.othersite.com/mypath?thing=stuff&otherthing=otherstuff", builder.toString());
    }

    @Test
    void queryWithNull() {
        final UrlBuilder builder = new UrlBuilder()
                .setQuery(null);
        assertEquals(0, builder.getQuery().size());
        assertEquals("", builder.toString());
    }

    @Test
    void queryWithEmpty() {
        final UrlBuilder builder = new UrlBuilder()
                .setQuery("");
        assertEquals(0, builder.getQuery().size());
        assertEquals("", builder.toString());
    }

    @Test
    void queryWithQuestionMark() {
        final UrlBuilder builder = new UrlBuilder()
                .setQuery("?");
        assertEquals(0, builder.getQuery().size());
        assertEquals("", builder.toString());
    }

    @Test
    void parseWithNullString() {
        final UrlBuilder builder = UrlBuilder.parse((String) null);
        assertEquals("", builder.toString());
    }

    @Test
    void parseWithEmpty() {
        final UrlBuilder builder = UrlBuilder.parse("");
        assertEquals("", builder.toString());
    }

    @Test
    void parseHost() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com");
        assertEquals("www.bing.com", builder.toString());
    }

    @Test
    void parseWithProtocolAndHost() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com");
        assertEquals("https://www.bing.com", builder.toString());
    }

    @Test
    void parseHostAndPort() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com:8080");
        assertEquals("www.bing.com:8080", builder.toString());
    }

    @Test
    void parseWithProtocolAndHostAndPort() {
        final UrlBuilder builder = UrlBuilder.parse("ftp://www.bing.com:8080");
        assertEquals("ftp://www.bing.com:8080", builder.toString());
    }

    @Test
    void parseHostAndPath() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com/my/path");
        assertEquals("www.bing.com/my/path", builder.toString());
    }

    @Test
    void parseWithProtocolAndHostAndPath() {
        final UrlBuilder builder = UrlBuilder.parse("ftp://www.bing.com/my/path");
        assertEquals("ftp://www.bing.com/my/path", builder.toString());
    }

    @Test
    void parseHostAndPortAndPath() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com:1234/my/path");
        assertEquals("www.bing.com:1234/my/path", builder.toString());
    }

    @Test
    void parseWithProtocolAndHostAndPortAndPath() {
        final UrlBuilder builder = UrlBuilder.parse("ftp://www.bing.com:2345/my/path");
        assertEquals("ftp://www.bing.com:2345/my/path", builder.toString());
    }

    @Test
    void parseHostAndOneQueryParameter() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com?a=1");
        assertEquals("www.bing.com?a=1", builder.toString());
    }

    @Test
    void parseWithProtocolAndHostAndOneQueryParameter() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com?a=1");
        assertEquals("https://www.bing.com?a=1", builder.toString());
    }

    @Test
    void parseHostAndPortAndOneQueryParameter() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com:123?a=1");
        assertEquals("www.bing.com:123?a=1", builder.toString());
    }

    @Test
    void parseWithProtocolAndHostAndPortAndOneQueryParameter() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com:987?a=1");
        assertEquals("https://www.bing.com:987?a=1", builder.toString());
    }

    @Test
    void parseHostAndPathAndOneQueryParameter() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com/folder/index.html?a=1");
        assertEquals("www.bing.com/folder/index.html?a=1", builder.toString());
    }

    @Test
    void parseWithProtocolAndHostAndPathAndOneQueryParameter() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com/image.gif?a=1");
        assertEquals("https://www.bing.com/image.gif?a=1", builder.toString());
    }

    @Test
    void parseHostAndPortAndPathAndOneQueryParameter() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com:123/index.html?a=1");
        assertEquals("www.bing.com:123/index.html?a=1", builder.toString());
    }

    @Test
    void parseWithProtocolAndHostAndPortAndPathAndOneQueryParameter() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com:987/my/path/again?a=1");
        assertEquals("https://www.bing.com:987/my/path/again?a=1", builder.toString());
    }

    @Test
    void parseHostAndTwoQueryParameters() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com?a=1&b=2");
        assertEquals("www.bing.com?a=1&b=2", builder.toString());
    }

    @Test
    void parseWithProtocolAndHostAndTwoQueryParameters() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com?a=1&b=2");
        assertEquals("https://www.bing.com?a=1&b=2", builder.toString());
    }

    @Test
    void parseHostAndPortAndTwoQueryParameters() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com:123?a=1&b=2");
        assertEquals("www.bing.com:123?a=1&b=2", builder.toString());
    }

    @Test
    void parseWithProtocolAndHostAndPortAndTwoQueryParameters() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com:987?a=1&b=2");
        assertEquals("https://www.bing.com:987?a=1&b=2", builder.toString());
    }

    @Test
    void parseHostAndPathAndTwoQueryParameters() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com/folder/index.html?a=1&b=2");
        assertEquals("www.bing.com/folder/index.html?a=1&b=2", builder.toString());
    }

    @Test
    void parseWithProtocolAndHostAndPathAndTwoQueryParameters() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com/image.gif?a=1&b=2");
        assertEquals("https://www.bing.com/image.gif?a=1&b=2", builder.toString());
    }

    @Test
    void parseHostAndPortAndPathAndTwoQueryParameters() {
        final UrlBuilder builder = UrlBuilder.parse("www.bing.com:123/index.html?a=1&b=2");
        assertEquals("www.bing.com:123/index.html?a=1&b=2", builder.toString());
    }

    @Test
    void parseWithProtocolAndHostAndPortAndPathAndTwoQueryParameters() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com:987/my/path/again?a=1&b=2");
        assertEquals("https://www.bing.com:987/my/path/again?a=1&b=2", builder.toString());
    }

    @Test
    void parseWithColonInPath() {
        final UrlBuilder builder = UrlBuilder.parse("https://www.bing.com/my:/path");
        assertEquals("https://www.bing.com/my:/path", builder.toString());
    }

    @Test
    void parseURLWithNull() {
        final UrlBuilder builder = UrlBuilder.parse((URL) null);
        assertEquals("", builder.toString());
    }

    @Test
    void parseURLSchemeAndHost() throws MalformedURLException {
        final UrlBuilder builder = UrlBuilder.parse(new URL("http://www.bing.com"));
        assertEquals("http://www.bing.com", builder.toString());
    }

    @Test
    void parallelParsing() throws InterruptedException {
        Thread.UncaughtExceptionHandler handler = mock(Thread.UncaughtExceptionHandler.class);
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors(),
                ForkJoinPool.defaultForkJoinWorkerThreadFactory, handler, false);

        AtomicInteger callCount = new AtomicInteger();
        List<Callable<UrlBuilder>> tasks = IntStream.range(0, 100000)
                .mapToObj(i -> (Callable<UrlBuilder>) () -> {
                    callCount.incrementAndGet();
                    return UrlBuilder.parse("https://example" + i + ".com");
                })
                .collect(Collectors.toList());

        pool.invokeAll(tasks);
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(100000, callCount.get());
    }

    @Test
    void fluxParallelParsing() {
        Mono<Long> mono = Flux.range(0, 100000)
                .parallel()
                .map(i -> UrlBuilder.parse("https://example" + i + ".com"))
                .sequential()
                .count();

        StepVerifier.create(mono)
                .assertNext(count -> assertEquals(100000, count))
                .verifyComplete();
    }
}