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
 * The pipeline policy that adds a given port to each {@link com.unisoft.core.http.HttpRequest}.
 *
 * @author omar.H.Ajmi
 * @since 19/10/2020
 */
public class PortPolicy implements HttpPipelinePolicy {

    private static final Logger log = LoggerFactory.getLogger(PortPolicy.class);
    private final int port;
    private final boolean overwrite;

    /**
     * Creates a new PortPolicy object.
     *
     * @param port      The port to set.
     * @param overwrite Whether or not to overwrite a {@link com.unisoft.core.http.HttpRequest HttpRequest's} port if it already has one.
     */
    public PortPolicy(int port, boolean overwrite) {
        this.port = port;
        this.overwrite = overwrite;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());
        if (overwrite || urlBuilder.getPort() == null) {
            LogUtil.info(log, "Changing port to " + port);

            try {
                context.getHttpRequest().setUrl(urlBuilder.setPort(port).toUrl());
            } catch (MalformedURLException e) {
                return Mono.error(new RuntimeException(
                        String.format("Failed to set the HTTP request port to %d.", port), e));
            }
        }
        return next.process();
    }
}
