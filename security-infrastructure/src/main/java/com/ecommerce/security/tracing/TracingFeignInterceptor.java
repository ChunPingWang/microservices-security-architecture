package com.ecommerce.security.tracing;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * Feign request interceptor that propagates trace context headers
 * across service-to-service calls.
 */
@Component
@ConditionalOnClass({Tracer.class, RequestInterceptor.class})
public class TracingFeignInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TracingFeignInterceptor.class);

    private static final String B3_TRACE_ID = "X-B3-TraceId";
    private static final String B3_SPAN_ID = "X-B3-SpanId";
    private static final String B3_PARENT_SPAN_ID = "X-B3-ParentSpanId";
    private static final String B3_SAMPLED = "X-B3-Sampled";

    private final Tracer tracer;

    public TracingFeignInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void apply(RequestTemplate template) {
        Span currentSpan = tracer.currentSpan();

        if (currentSpan != null) {
            String traceId = currentSpan.context().traceId();
            String spanId = currentSpan.context().spanId();

            template.header(B3_TRACE_ID, traceId);
            template.header(B3_SPAN_ID, spanId);
            template.header(B3_SAMPLED, "1");

            log.debug("Propagating trace context: traceId={}, spanId={}", traceId, spanId);
        } else {
            log.debug("No active span found, skipping trace propagation");
        }
    }
}
