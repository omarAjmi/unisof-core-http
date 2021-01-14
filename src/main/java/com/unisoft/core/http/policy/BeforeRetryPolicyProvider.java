package com.unisoft.core.http.policy;

/**
 * Implementing classes are automatically added as policies before the retry policy.
 *
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
public interface BeforeRetryPolicyProvider extends HttpPolicyProvider {
}
