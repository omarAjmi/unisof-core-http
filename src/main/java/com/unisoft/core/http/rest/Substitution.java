package com.unisoft.core.http.rest;

/**
 * A Substitution is a value that can be used to replace placeholder values in a URL. Placeholders
 * look like: "http://{host}.com/{fileName}.html", where "{host}" and "{fileName}" are the
 * placeholders.
 *
 * @author omar.H.Ajmi
 * @since 16/01/2021
 */
class Substitution {
    private final String urlParameterName;
    private final int methodParameterIndex;
    private final boolean shouldEncode;

    /**
     * Create a new Substitution.
     *
     * @param urlParameterName     The name that is used between curly quotes as a placeholder in the
     *                             target URL.
     * @param methodParameterIndex The index of the parameter in the original interface method where
     *                             the value for the placeholder is.
     * @param shouldEncode         Whether or not the value from the method's argument should be encoded
     *                             when the substitution is taking place.
     */
    Substitution(String urlParameterName, int methodParameterIndex, boolean shouldEncode) {
        this.urlParameterName = urlParameterName;
        this.methodParameterIndex = methodParameterIndex;
        this.shouldEncode = shouldEncode;
    }

    /**
     * Get the placeholder's name.
     *
     * @return The name of the placeholder.
     */
    public String getUrlParameterName() {
        return this.urlParameterName;
    }

    /**
     * Get the index of the method parameter where the replacement value is.
     *
     * @return The index of the method parameter where the replacement value is.
     */
    public int getMethodParameterIndex() {
        return this.methodParameterIndex;
    }

    /**
     * Get whether or not the replacement value from the method argument needs to be encoded when the
     * substitution is taking place.
     *
     * @return Whether or not the replacement value from the method argument needs to be encoded
     * when the substitution is taking place.
     */
    public boolean shouldEncode() {
        return this.shouldEncode;
    }
}
