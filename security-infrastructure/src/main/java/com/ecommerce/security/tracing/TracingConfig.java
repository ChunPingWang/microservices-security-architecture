package com.ecommerce.security.tracing;

import brave.Tracing;
import brave.propagation.B3Propagation;
import brave.propagation.Propagation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for distributed tracing using Micrometer and Brave.
 * This configuration ensures trace context propagation across service boundaries.
 */
@Configuration
@ConditionalOnClass(Tracing.class)
public class TracingConfig {

    /**
     * Configures B3 propagation for distributed tracing.
     * B3 is a standard header format used by Zipkin for trace propagation.
     *
     * Headers propagated:
     * - X-B3-TraceId: The overall trace ID
     * - X-B3-SpanId: The current span ID
     * - X-B3-ParentSpanId: The parent span ID
     * - X-B3-Sampled: Whether this trace should be sampled
     */
    @Bean
    public Propagation.Factory propagationFactory() {
        return B3Propagation.newFactoryBuilder()
                .injectFormat(B3Propagation.Format.MULTI)
                .build();
    }
}
