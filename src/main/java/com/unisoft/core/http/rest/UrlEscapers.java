package com.unisoft.core.http.rest;

import com.google.common.net.PercentEscaper;

/**
 * Collection of useful URL escapers.
 *
 * @author omar.H.Ajmi
 * @since 16/01/2021
 */
final class UrlEscapers {

    private static final String UNRESERVED_SYMBOLS = "-._~";
    /**
     * An escaper for escaping query parameters.
     */
    public static final PercentEscaper QUERY_ESCAPER = new PercentEscaper(UNRESERVED_SYMBOLS + "/?", false);
    /**
     * An escaper for escaping form parameters.
     */
    public static final PercentEscaper FORM_ESCAPER = new PercentEscaper(UNRESERVED_SYMBOLS, true);
    private static final String SUB_DELIMS = "!$&'()*+,;=";
    /**
     * An escaper for escaping path parameters.
     */
    public static final PercentEscaper PATH_ESCAPER = new PercentEscaper(UNRESERVED_SYMBOLS + SUB_DELIMS + ":@", false);

}