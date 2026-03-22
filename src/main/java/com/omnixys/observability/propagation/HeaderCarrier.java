package com.omnixys.observability.propagation;

public interface HeaderCarrier {

    void set(String key, String value);

    String get(String key);
}