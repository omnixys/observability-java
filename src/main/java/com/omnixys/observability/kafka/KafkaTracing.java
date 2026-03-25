package com.omnixys.observability.kafka;

import java.util.function.Supplier;

public interface KafkaTracing {

    void inject(Object carrier);

    <T> T produce(String topic, Supplier<T> fn);
}