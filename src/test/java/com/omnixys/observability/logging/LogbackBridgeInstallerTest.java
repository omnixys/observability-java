package com.omnixys.observability.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterAttachable;
import com.omnixys.observability.properties.ObservabilityProperties;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class LogbackBridgeInstallerTest {

    private final LoggerContext context = new LoggerContext();
    private final ObservabilityProperties properties = new ObservabilityProperties();

    @Test
    void installsOtelAppenderWithDebugThreshold() {
        ConsoleAppender<ILoggingEvent> console = consoleAppender("CONSOLE");
        context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(console);
        context.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.INFO);

        LogbackBridgeInstaller.install(OpenTelemetry.noop(), properties, "demo", context);

        var otelAppender = context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("OMNIXYS_OTEL");
        assertNotNull(otelAppender);
        assertEquals(FilterReply.NEUTRAL, decide(otelAppender, Level.DEBUG));
        assertEquals(FilterReply.NEUTRAL, decide(otelAppender, Level.INFO));
        assertEquals(FilterReply.DENY, decide(otelAppender, Level.TRACE));
    }

    @Test
    void keepsConsoleQuietForDebugWhileAllowingInfo() {
        ConsoleAppender<ILoggingEvent> console = consoleAppender("CONSOLE");
        context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(console);
        context.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.INFO);

        LogbackBridgeInstaller.install(OpenTelemetry.noop(), properties, "demo", context);

        assertEquals(FilterReply.DENY, decide(console, Level.DEBUG));
        assertEquals(FilterReply.NEUTRAL, decide(console, Level.INFO));
    }

    @Test
    void installIsIdempotent() {
        ConsoleAppender<ILoggingEvent> console = consoleAppender("CONSOLE");
        context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(console);

        LogbackBridgeInstaller.install(OpenTelemetry.noop(), properties, "demo", context);
        var first = context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("OMNIXYS_OTEL");
        LogbackBridgeInstaller.install(OpenTelemetry.noop(), properties, "demo", context);
        var second = context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("OMNIXYS_OTEL");

        assertSame(first, second);
        assertEquals(1, countThresholdFilters(console));
    }

    @Test
    void lowersServicePackageLoggerToDebugWhenInheritedLevelIsHigher() {
        context.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.INFO);
        assertNull(context.getLogger("com.omnixys.demo").getLevel());

        LogbackBridgeInstaller.install(OpenTelemetry.noop(), properties, "demo", context);

        assertEquals(Level.DEBUG, context.getLogger("com.omnixys.demo").getLevel());
        assertEquals(Level.DEBUG, context.getLogger("com.omnixys.demo").getEffectiveLevel());
        assertEquals(Level.INFO, context.getLogger(Logger.ROOT_LOGGER_NAME).getLevel());
    }

    @Test
    void keepsMoreVerboseTraceLevelUntouched() {
        context.getLogger("com.omnixys.demo").setLevel(Level.TRACE);

        LogbackBridgeInstaller.install(OpenTelemetry.noop(), properties, "demo", context);

        assertEquals(Level.TRACE, context.getLogger("com.omnixys.demo").getLevel());
    }

    @Test
    void explicitLoggerNamePropertyWinsOverDerivedName() {
        properties.getLogs().setLoggerName("com.example.custom");

        LogbackBridgeInstaller.install(OpenTelemetry.noop(), properties, "demo", context);

        assertEquals(Level.DEBUG, context.getLogger("com.example.custom").getLevel());
        assertNull(context.exists("com.omnixys.demo"));
    }

    @Test
    void skipsPackageRaiseWhenNoNameIsResolvable() {
        LogbackBridgeInstaller.install(OpenTelemetry.noop(), properties, null, context);

        assertNotNull(context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("OMNIXYS_OTEL"));
        assertNull(context.exists("com.omnixys.unknown-service"));

        LogbackBridgeInstaller.install(OpenTelemetry.noop(), properties, "unknown-service", context);

        assertNull(context.exists("com.omnixys.unknown"));
    }

    @Test
    void derivesLoggerNameByStrippingServiceSuffix() {
        assertEquals("com.omnixys.address",
                LogbackBridgeInstaller.resolveLoggerName(properties, "address-service"));
        assertEquals("com.omnixys.account",
                LogbackBridgeInstaller.resolveLoggerName(properties, "account"));
        assertNull(LogbackBridgeInstaller.resolveLoggerName(properties, "unknown-service"));
        assertNull(LogbackBridgeInstaller.resolveLoggerName(properties, null));
    }

    @Test
    void invalidLevelsFallBackToDefaults() {
        properties.getLogs().setLevel("banana");
        properties.getLogs().setConsoleThreshold("also-banana");

        ConsoleAppender<ILoggingEvent> console = consoleAppender("CONSOLE");
        context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(console);
        context.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.INFO);

        LogbackBridgeInstaller.install(OpenTelemetry.noop(), properties, "demo", context);

        var otelAppender = context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("OMNIXYS_OTEL");
        assertEquals(FilterReply.DENY, decide(otelAppender, Level.TRACE));
        assertEquals(FilterReply.DENY, decide(console, Level.DEBUG));
        assertEquals(FilterReply.NEUTRAL, decide(console, Level.INFO));
    }

    private ConsoleAppender<ILoggingEvent> consoleAppender(String name) {
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(context);
        appender.setName(name);
        appender.start();
        return appender;
    }

    private FilterReply decide(Appender<ILoggingEvent> appender, Level level) {
        LoggingEvent event = new LoggingEvent(
                "com.omnixys.observability.Test",
                context.getLogger("com.omnixys.demo"),
                level,
                "message",
                null,
                new Object[0]
        );
        return ((FilterAttachable<ILoggingEvent>) appender).getFilterChainDecision(event);
    }

    private int countThresholdFilters(FilterAttachable<ILoggingEvent> attachable) {
        int count = 0;
        for (Filter<ILoggingEvent> filter : attachable.getCopyOfAttachedFiltersList()) {
            if (filter instanceof ThresholdFilter) count++;
        }
        return count;
    }
}
