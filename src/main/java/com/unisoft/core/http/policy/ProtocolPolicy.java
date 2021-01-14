package com.unisoft.core.http.policy;

import com.unisoft.core.http.HttpPipelineCallContext;
import com.unisoft.core.http.HttpPipelineNextPolicy;
import com.unisoft.core.http.HttpResponse;
import com.unisoft.core.http.util.UrlBuilder;
import com.unisoft.core.util.log.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;

/**
 * the pipeline policy that adds a given protocol to each HttpRequest.
 *
 * @author omar.H.Ajmi
 * @since 19/10/2020
 */
public class ProtocolPolicy implements HttpPipelinePolicy {

    private static final Logger log = LoggerFactory.getLogger(ProtocolPolicy.class);

    private final String protocol;
    private final boolean overwrite;

    /**
     * Creates a new ProtocolPolicy.
     *
     * @param protocol  The protocol to set.
     * @param overwrite Whether or not to overwrite a HttpRequest's protocol if it already has one.
     */
    public ProtocolPolicy(String protocol, boolean overwrite) {
        this.protocol = protocol;
        this.overwrite = overwrite;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());
        if (overwrite || urlBuilder.getScheme() == null) {
            LogUtil.info(log, "Setting protocol to " + protocol);

            try {
                context.getHttpRequest().setUrl(urlBuilder.setScheme(protocol).toUrl());
            } catch (MalformedURLException e) {
                return Mono.error(new RuntimeException(
                        String.format("Failed to set the HTTP request protocol to %s.", protocol), e));
            }
        }
        return next.process();
    }
}
