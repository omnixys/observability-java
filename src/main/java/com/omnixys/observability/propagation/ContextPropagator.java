package com.omnixys.observability.propagation;

import com.omnixys.observability.context.ITraceContext;

public interface ContextPropagator {

    void inject(ITraceContext context, HeaderCarrier carrier);

    ITraceContext extract(HeaderCarrier carrier);
}
