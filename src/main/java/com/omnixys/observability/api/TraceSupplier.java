package com.omnixys.observability.api;

@FunctionalInterface
public interface TraceSupplier<T> {
    T get() throws Exception;
}