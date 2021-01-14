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
 * The pipeline policy that adds the given host to each HttpRequest.
 *
 * @author omar.H.Ajmi
 * @since 19/10/2020
 */
public class HostPolicy implements HttpPipelinePolicy {

    private final static Logger log = LoggerFactory.getLogger(HostPolicy.class);

    private final String host;

    /**
     * Create HostPolicy.
     *
     * @param host The host to set on every HttpRequest.
     */
    public HostPolicy(String host) {
        this.host = host;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        LogUtil.info(log, "Setting host to " + host);

        Mono<HttpResponse> result;
        final UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());
        try {
            context.getHttpRequest().setUrl(urlBuilder.setHost(host).toUrl());
            result = next.process();
        } catch (MalformedURLException e) {
            result = Mono.error(new RuntimeException(String.format("Host URL '%s' is invalid.",
                    host), e));
        }
        return result;
    }
}
