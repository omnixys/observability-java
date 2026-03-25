package com.omnixys.observability.http;

import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        prefix = "omnixys.observability.tracing.http",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class HttpTracingAutoConfiguration {

    @Bean
    public FilterRegistrationBean<HttpTracingFilter> httpTracingFilter() {
        FilterRegistrationBean<HttpTracingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new HttpTracingFilter());
        registration.setOrder(1); // wichtig: früh!
        registration.addUrlPatterns("/*");
        return registration;
    }
}