package com.omnixys.observability.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterAttachable;
import com.omnixys.observability.properties.ObservabilityProperties;
import io.opentelemetry.api.OpenTelemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Installs exactly one OTel appender on the root Logback logger.
 *
 * <p>Additionally decouples the console from the OTel logs signal: debug
 * records of the service package reach Loki while console appenders keep a
 * higher threshold (INFO by default) so production consoles stay quiet.</p>
 */
public final class LogbackBridgeInstaller {
    private static final String APPENDER_NAME = "OMNIXYS_OTEL";
    private static final String SERVICE_SUFFIX = "-service";

    private LogbackBridgeInstaller() {
    }

    public static void install(
            OpenTelemetry openTelemetry,
            ObservabilityProperties properties,
            String applicationName
    ) {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext context)) return;
        install(openTelemetry, properties, applicationName, context);
    }

    static void install(
            OpenTelemetry openTelemetry,
            ObservabilityProperties properties,
            String applicationName,
            LoggerContext context
    ) {
        Level otelLevel = Level.toLevel(properties.getLogs().getLevel(), Level.DEBUG);
        Level consoleLevel = Level.toLevel(properties.getLogs().getConsoleThreshold(), Level.INFO);

        ch.qos.logback.classic.Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        if (root.getAppender(APPENDER_NAME) == null) {
            OpenTelemetryLogbackAppender appender = new OpenTelemetryLogbackAppender(openTelemetry);
            appender.setContext(context);
            appender.setName(APPENDER_NAME);
            addThreshold(appender, otelLevel, context);
            appender.start();
            root.addAppender(appender);
        }

        ensureConsoleThresholds(context, consoleLevel);
        ensureDebugVisibility(context, resolveLoggerName(properties, applicationName));
    }

    private static void ensureConsoleThresholds(LoggerContext context, Level threshold) {
        for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
            for (Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders(); it.hasNext(); ) {
                Appender<ILoggingEvent> appender = it.next();
                if (appender instanceof ConsoleAppender && !hasThreshold(appender)) {
                    addThreshold(cast(appender), threshold, context);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static FilterAttachable<ILoggingEvent> cast(Appender<ILoggingEvent> appender) {
        return (FilterAttachable<ILoggingEvent>) appender;
    }

    private static void addThreshold(FilterAttachable<ILoggingEvent> attachable,
                                     Level level,
                                     LoggerContext context) {
        ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(level.toString());
        filter.setContext(context);
        filter.start();
        attachable.addFilter(filter);
    }

    private static boolean hasThreshold(Appender<ILoggingEvent> appender) {
        return hasThreshold(cast(appender));
    }

    private static boolean hasThreshold(FilterAttachable<ILoggingEvent> attachable) {
        for (Filter<ILoggingEvent> filter : attachable.getCopyOfAttachedFiltersList()) {
            if (filter instanceof ThresholdFilter) return true;
        }
        return false;
    }

    private static void ensureDebugVisibility(LoggerContext context, String loggerName) {
        if (loggerName == null || loggerName.isBlank()
                || Logger.ROOT_LOGGER_NAME.equalsIgnoreCase(loggerName)) {
            return;
        }
        ch.qos.logback.classic.Logger logger = context.getLogger(loggerName);
        Level current = logger.getLevel() != null ? logger.getLevel() : logger.getEffectiveLevel();
        if (current == null) return;
        if (current.isGreaterOrEqual(Level.DEBUG)) {
            logger.setLevel(Level.DEBUG);
        }
    }

    static String resolveLoggerName(ObservabilityProperties properties, String applicationName) {
        String explicit = properties.getLogs().getLoggerName();
        if (explicit != null && !explicit.isBlank()) return explicit.trim();

        if (applicationName == null || applicationName.isBlank()
                || "unknown-service".equalsIgnoreCase(applicationName.trim())) {
            return null;
        }
        String normalized = applicationName.trim().toLowerCase();
        if (normalized.endsWith(SERVICE_SUFFIX)) {
            normalized = normalized.substring(0, normalized.length() - SERVICE_SUFFIX.length());
        }
        return "com.omnixys." + normalized;
    }
}
