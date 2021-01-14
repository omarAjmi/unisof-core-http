package com.unisoft.core.http.policy;

/**
 * Implementing classes automatically provide policies.
 *
 * @author omar.H.Ajmi
 * @since 18/10/2020
 */
@FunctionalInterface
public interface HttpPolicyProvider {

    /**
     * Creates the policy.
     *
     * @return the policy that was created.
     */
    HttpPipelinePolicy create();
}
