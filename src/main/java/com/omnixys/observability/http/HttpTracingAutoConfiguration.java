package com.omnixys.observability.http;

import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        prefix = "omnixys.observability.tracing",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class HttpTracingAutoConfiguration {

    @Bean
    public Filter httpTracingFilter() {
        return new HttpTracingFilter();
    }
}