package com.omnixys.observability.logging;

import ch.qos.logback.classic.LoggerContext;
import io.opentelemetry.api.OpenTelemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Installs exactly one OTel appender on the root Logback logger. */
public final class LogbackBridgeInstaller {
    private static final String APPENDER_NAME = "OMNIXYS_OTEL";

    private LogbackBridgeInstaller() {
    }

    public static void install(OpenTelemetry openTelemetry) {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext context)) return;
        ch.qos.logback.classic.Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        if (root.getAppender(APPENDER_NAME) != null) return;

        OpenTelemetryLogbackAppender appender = new OpenTelemetryLogbackAppender(openTelemetry);
        appender.setContext(context);
        appender.setName(APPENDER_NAME);
        appender.start();
        root.addAppender(appender);
    }
}
