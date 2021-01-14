package com.unisoft.core.http.util;

/**
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
enum UrlTokenizerState {
    SCHEME,

    SCHEME_OR_HOST,

    HOST,

    PORT,

    PATH,

    QUERY,

    DONE
}
