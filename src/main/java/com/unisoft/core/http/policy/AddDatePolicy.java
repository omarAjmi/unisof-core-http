package com.unisoft.core.http.policy;

import com.unisoft.core.http.HttpPipelineCallContext;
import com.unisoft.core.http.HttpPipelineNextPolicy;
import com.unisoft.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * The pipeline policy that adds a "Date" header in RFC 1123 format when sending an HTTP request.
 *
 * @author omar.H.Ajmi
 * @since 19/10/2020
 */
public class AddDatePolicy implements HttpPipelinePolicy {
    private final DateTimeFormatter format = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
            .withZone(ZoneId.of("UTC"))
            .withLocale(Locale.US);

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return Mono.defer(() -> {
            context.getHttpRequest().getHeaders().put("Date", format.format(OffsetDateTime.now()));
            return next.process();
        });
    }
}
