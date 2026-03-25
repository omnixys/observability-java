package com.omnixys.observability.api;

public interface TraceScope extends AutoCloseable {

    @Override
    void close();
}