package com.omnixys.observability;

import com.omnixys.context.ContextAccessor;
import com.omnixys.context.filter.ContextFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain chain;

    @Test
    void shouldReadCorrelationIdAndSetOnContextAccessor() throws IOException, ServletException {
        when(request.getHeader("X-Request-Id")).thenReturn("req-1");
        when(request.getHeader("X-Correlation-Id")).thenReturn("corr-1");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getServerName()).thenReturn("localhost");

        var filter = new ContextFilter();
        filter.doFilter(request, response, (req, res) -> {
            var ctx = ContextAccessor.get();
            assertNotNull(ctx);
            assertEquals("corr-1", ctx.correlationId());
            assertEquals("req-1", ctx.requestId());
        });

        assertNull(ContextAccessor.get());
    }

    @Test
    void shouldFallbackToRequestIdWhenCorrelationIdMissing() throws IOException, ServletException {
        when(request.getHeader("X-Request-Id")).thenReturn("req-1");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getServerName()).thenReturn("localhost");

        var filter = new ContextFilter();
        filter.doFilter(request, response, (req, res) -> {
            var ctx = ContextAccessor.get();
            assertNotNull(ctx);
            assertEquals("req-1", ctx.correlationId());
        });
    }

    @Test
    void shouldGenerateUuidWhenBothHeadersMissing() throws IOException, ServletException {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getServerName()).thenReturn("localhost");

        var filter = new ContextFilter();
        filter.doFilter(request, response, (req, res) -> {
            var ctx = ContextAccessor.get();
            assertNotNull(ctx);
            assertNotNull(ctx.requestId());
            assertEquals(ctx.requestId(), ctx.correlationId());
        });
    }

    @Test
    void shouldClearContextOnCompletion() throws IOException, ServletException {
        when(request.getHeader("X-Request-Id")).thenReturn("req-1");
        when(request.getHeader("X-Correlation-Id")).thenReturn("corr-1");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getServerName()).thenReturn("localhost");

        var filter = new ContextFilter();
        filter.doFilter(request, response, chain);

        assertNull(ContextAccessor.get());
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldClearContextOnException() {
        when(request.getHeader("X-Request-Id")).thenReturn("req-1");
        when(request.getHeader("X-Correlation-Id")).thenReturn("corr-1");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getServerName()).thenReturn("localhost");

        var filter = new ContextFilter();
        assertThrows(RuntimeException.class, () ->
                filter.doFilter(request, response, (req, res) -> {
                    throw new RuntimeException("boom");
                })
        );

        assertNull(ContextAccessor.get());
    }
}
