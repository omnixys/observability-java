package com.omnixys.observability.graphql;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        prefix = "omnixys.observability.tracing.graphql",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class GraphqlTracingAutoConfiguration {

    @Bean
    public GraphQLTracingInstrumentation graphQLTracingInstrumentation() {
        return new GraphQLTracingInstrumentation();
    }
}
