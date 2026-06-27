package com.omnixys.observability.logging;

public final class ErrorClassifier {

    private ErrorClassifier() {}

    public static String classify(Throwable err) {
        if (err == null) return "unknown";
        if (err instanceof RuntimeException) return "internal_error";
        return "unknown";
    }

    public static String classify(int httpStatus) {
        if (httpStatus >= 500) return "server_error";
        if (httpStatus >= 400) return "client_error";
        return "unknown";
    }
}
