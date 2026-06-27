package com.omnixys.observability.propagation;

@Deprecated(forRemoval = true)
public interface HeaderCarrier {

    void set(String key, String value);

    String get(String key);
}