package com.omnixys.observability.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;

import java.util.Map;
import java.util.regex.Pattern;

/** Bridges regular SLF4J/Logback records to the OTel logs signal. */
public final class OpenTelemetryLogbackAppender extends AppenderBase<ILoggingEvent> {

    private static final Pattern SENSITIVE =
            Pattern.compile("(authorization|cookie|password|secret|token|api[-_]?key)", Pattern.CASE_INSENSITIVE);

    private final OpenTelemetry openTelemetry;

    public OpenTelemetryLogbackAppender(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Override
    protected void append(ILoggingEvent event) {
        try {
            LogRecordBuilder record = openTelemetry.getLogsBridge()
                    .loggerBuilder(event.getLoggerName())
                    .build()
                    .logRecordBuilder()
                    .setContext(Context.current())
                    .setTimestamp(event.getInstant())
                    .setSeverity(toSeverity(event.getLevel()))
                    .setSeverityText(event.getLevel().levelStr)
                    .setBody(event.getFormattedMessage())
                    .setAttribute(io.opentelemetry.api.common.AttributeKey.stringKey("logger.name"), event.getLoggerName());

            for (Map.Entry<String, String> entry : event.getMDCPropertyMap().entrySet()) {
                if (!SENSITIVE.matcher(entry.getKey()).find()) {
                    record.setAttribute(
                            io.opentelemetry.api.common.AttributeKey.stringKey(canonicalKey(entry.getKey())),
                            entry.getValue()
                    );
                }
            }

            if (event.getThrowableProxy() != null) {
                record.setAttribute(
                        io.opentelemetry.api.common.AttributeKey.stringKey("exception.stacktrace"),
                        ThrowableProxyUtil.asString(event.getThrowableProxy())
                );
                record.setAttribute(
                        io.opentelemetry.api.common.AttributeKey.stringKey("exception.type"),
                        event.getThrowableProxy().getClassName()
                );
                record.setAttribute(
                        io.opentelemetry.api.common.AttributeKey.stringKey("exception.message"),
                        String.valueOf(event.getThrowableProxy().getMessage())
                );
            }
            record.emit();
        } catch (RuntimeException ignored) {
            // Telemetry failures must never affect application control flow.
        }
    }

    private static Severity toSeverity(Level level) {
        if (level.isGreaterOrEqual(Level.ERROR)) return Severity.ERROR;
        if (level.isGreaterOrEqual(Level.WARN)) return Severity.WARN;
        if (level.isGreaterOrEqual(Level.INFO)) return Severity.INFO;
        if (level.isGreaterOrEqual(Level.DEBUG)) return Severity.DEBUG;
        return Severity.TRACE;
    }

    private static String canonicalKey(String key) {
        return switch (key) {
            case "requestId" -> "request.id";
            case "correlationId" -> "correlation.id";
            case "actorId" -> "actor.id";
            case "tenantId" -> "tenant.id";
            case "organizationId" -> "organization.id";
            default -> key;
        };
    }
}
