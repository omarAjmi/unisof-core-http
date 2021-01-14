package com.unisoft.core.http.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UrlTokenizerTest {

    private static void nextTest(String text, UrlToken... expectedTokens) {
        final UrlTokenizer tokenizer = new UrlTokenizer(text);
        final List<UrlToken> tokenList = new ArrayList<>();
        while (tokenizer.next()) {
            tokenList.add(tokenizer.current());
        }
        final UrlToken[] tokenArray = new UrlToken[tokenList.size()];
        tokenList.toArray(tokenArray);
        assertArrayEquals(expectedTokens, tokenArray);

        assertFalse(tokenizer.next());
        assertNull(tokenizer.current());
    }

    @Test
    void constructor() {
        final UrlTokenizer tokenizer = new UrlTokenizer("http://www.bing.com");
        assertNull(tokenizer.current());
    }

    @Test
    void nextWithNullText() {
        final UrlTokenizer tokenizer = new UrlTokenizer(null);
        assertFalse(tokenizer.next());
        assertNull(tokenizer.current());
    }

    @Test
    void nextWithEmptyText() {
        final UrlTokenizer tokenizer = new UrlTokenizer("");
        assertFalse(tokenizer.next());
        assertNull(tokenizer.current());
    }

    @Test
    void nextWithSchemeButNoSeparator() {
        nextTest("http", UrlToken.host("http"));
    }

    @Test
    void nextWithSchemeAndColon() {
        nextTest("http:",
                UrlToken.host("http"),
                UrlToken.port(""));
    }

    @Test
    void nextWithSchemeAndColonAndForwardSlash() {
        nextTest("http:/",
                UrlToken.host("http"),
                UrlToken.port(""),
                UrlToken.path("/"));
    }

    @Test
    void nextWithSchemeAndColonAndTwoForwardSlashes() {
        nextTest("http://",
                UrlToken.scheme("http"),
                UrlToken.host(""));
    }

    @Test
    void nextWithSchemeAndHost() {
        nextTest("https://www.example.com",
                UrlToken.scheme("https"),
                UrlToken.host("www.example.com"));
    }

    @Test
    void nextWithSchemeAndHostAndColon() {
        nextTest("https://www.example.com:",
                UrlToken.scheme("https"),
                UrlToken.host("www.example.com"),
                UrlToken.port(""));
    }

    @Test
    void nextWithSchemeAndHostAndPort() {
        nextTest("https://www.example.com:8080",
                UrlToken.scheme("https"),
                UrlToken.host("www.example.com"),
                UrlToken.port("8080"));
    }

    @Test
    void nextWithSchemeAndHostAndPortAndForwardSlash() {
        nextTest("ftp://www.bing.com:123/",
                UrlToken.scheme("ftp"),
                UrlToken.host("www.bing.com"),
                UrlToken.port("123"),
                UrlToken.path("/"));
    }

    @Test
    void nextWithSchemeAndHostAndPortAndPath() {
        nextTest("ftp://www.bing.com:123/a/b/c.txt",
                UrlToken.scheme("ftp"),
                UrlToken.host("www.bing.com"),
                UrlToken.port("123"),
                UrlToken.path("/a/b/c.txt"));
    }

    @Test
    void nextWithSchemeAndHostAndPortAndQuestionMark() {
        nextTest("ftp://www.bing.com:123?",
                UrlToken.scheme("ftp"),
                UrlToken.host("www.bing.com"),
                UrlToken.port("123"),
                UrlToken.query(""));
    }

    @Test
    void nextWithSchemeAndHostAndPortAndQuery() {
        nextTest("ftp://www.bing.com:123?a=b&c=d",
                UrlToken.scheme("ftp"),
                UrlToken.host("www.bing.com"),
                UrlToken.port("123"),
                UrlToken.query("a=b&c=d"));
    }

    @Test
    void nextWithSchemeAndHostAndForwardSlash() {
        nextTest("https://www.example.com/",
                UrlToken.scheme("https"),
                UrlToken.host("www.example.com"),
                UrlToken.path("/"));
    }

    @Test
    void nextWithSchemeAndHostAndPath() {
        nextTest("https://www.example.com/index.html",
                UrlToken.scheme("https"),
                UrlToken.host("www.example.com"),
                UrlToken.path("/index.html"));
    }

    @Test
    void nextWithSchemeAndHostAndPathAndQuestionMark() {
        nextTest("https://www.example.com/index.html?",
                UrlToken.scheme("https"),
                UrlToken.host("www.example.com"),
                UrlToken.path("/index.html"),
                UrlToken.query(""));
    }

    @Test
    void nextWithSchemeAndHostAndPathAndQuery() {
        nextTest("https://www.example.com/index.html?alpha=beta",
                UrlToken.scheme("https"),
                UrlToken.host("www.example.com"),
                UrlToken.path("/index.html"),
                UrlToken.query("alpha=beta"));
    }

    @Test
    void nextWithSchemeAndHostAndQuestionMark() {
        nextTest("https://www.example.com?",
                UrlToken.scheme("https"),
                UrlToken.host("www.example.com"),
                UrlToken.query(""));
    }

    @Test
    void nextWithSchemeAndHostAndQuery() {
        nextTest("https://www.example.com?a=b",
                UrlToken.scheme("https"),
                UrlToken.host("www.example.com"),
                UrlToken.query("a=b"));
    }

    @Test
    void nextWithHostAndForwardSlash() {
        nextTest("www.test.com/",
                UrlToken.host("www.test.com"),
                UrlToken.path("/"));
    }

    @Test
    void nextWithHostAndQuestionMark() {
        nextTest("www.test.com?",
                UrlToken.host("www.test.com"),
                UrlToken.query(""));
    }

    @Test
    void nextWithPath() {
        nextTest("folder/index.html",
                UrlToken.host("folder"),
                UrlToken.path("/index.html"));
    }

    @Test
    void nextWithForwardSlashAndPath() {
        nextTest("/folder/index.html",
                UrlToken.host(""),
                UrlToken.path("/folder/index.html"));
    }
}