package com.omnixys.observability.bridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class ObservabilityEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String prometheusEnabled = environment.getProperty("omnixys.observability.metrics.prometheus-enabled", "true");

        if (!Boolean.parseBoolean(prometheusEnabled)) {
            return;
        }

        Map<String, Object> props = new HashMap<>();

        if (!environment.containsProperty("management.endpoint.prometheus.enabled")) {
            props.put("management.endpoint.prometheus.enabled", true);
        }

        if (!environment.containsProperty("management.endpoints.web.exposure.include")) {
            props.put("management.endpoints.web.exposure.include", "health,info,metrics,prometheus");
        }

        if (!props.isEmpty()) {
            environment.getPropertySources().addLast(
                    new MapPropertySource("omnixys-observability-bridge", props)
            );
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
