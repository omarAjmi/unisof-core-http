package com.unisoft.core.http;

import com.unisoft.core.http.policy.HttpPipelinePolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link HttpPipeline},
 * calling {@link HttpPipelineBuilder#build() build} constructs an instance of the pipeline.
 *
 * <p>A pipeline is configured with a HttpClient that sends the request, if no client is set a default is used.
 * A pipeline may be configured with a list of policies that are applied to each request.</p>
 *
 * <p><strong>Code Samples</strong></p>
 *
 * <p>Create a pipeline without configuration</p>
 *
 * <pre>
 * new HttpPipelineBuilder()
 *     .build();
 * </pre>
 *
 * <p>Create a pipeline using the default HTTP client and a retry policy</p>
 *
 * <pre>
 * new HttpPipelineBuilder()
 *     .httpClient(HttpClient.createDefault())
 *     .policies(new RetryPolicy())
 *     .build();
 * </pre>
 *
 * @author omar.H.Ajmi
 * @since 19/10/2020
 */
public class HttpPipelineBuilder {
    private HttpClient httpClient;
    private List<HttpPipelinePolicy> pipelinePolicies;

    /**
     * Creates a new instance of HttpPipelineBuilder that can configure options for the {@link HttpPipeline} before
     * creating an instance of it.
     */
    public HttpPipelineBuilder() {
        //no-op
    }

    /**
     * Creates a {@link HttpPipeline} based on options set in the Builder. Every time {@code build()} is
     * called, a new instance of {@link HttpPipeline} is created.
     * <p>
     * If HttpClient is not set then the {@link HttpClient#createDefault() default HttpClient} is used.
     *
     * @return A HttpPipeline with the options set from the builder.
     */
    public HttpPipeline build() {
        List<HttpPipelinePolicy> policies = (pipelinePolicies == null) ? new ArrayList<>() : pipelinePolicies;
        HttpClient client = (httpClient == null) ? HttpClient.createDefault() : httpClient;

        return new HttpPipeline(client, policies);
    }

    /**
     * Sets the HttpClient that the pipeline will use to send requests.
     *
     * @param httpClient The HttpClient the pipeline will use when sending requests.
     * @return The updated HttpPipelineBuilder object.
     */
    public HttpPipelineBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds {@link HttpPipelinePolicy policies} to the set of policies that the pipeline will use
     * when sending requests.
     *
     * @param policies Policies to add to the policy set.
     * @return The updated HttpPipelineBuilder object.
     */
    public HttpPipelineBuilder policies(HttpPipelinePolicy... policies) {
        if (pipelinePolicies == null) {
            pipelinePolicies = new ArrayList<>();
        }

        this.pipelinePolicies.addAll(Arrays.asList(policies));
        return this;
    }
}
