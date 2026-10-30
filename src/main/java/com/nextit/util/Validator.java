package com.nextit.util;

import com.nextit.exception.AppException;

public final class Validator {

    private Validator() {}

    public static void notBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new AppException(fieldName + " is required.");
        }
    }

    public static void email(String value) {
        if (value == null || !value.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new AppException("A valid email address is required.");
        }
    }

    /** Allows an empty value but rejects text that is not an http(s) URL. */
    public static void optionalWebUrl(String value, String fieldName) {
        if (value != null && !value.isBlank() && !value.trim().matches("^https?://\\S+$")) {
            throw new AppException(fieldName + " must start with http:// or https://.");
        }
    }

    public static void nonNegative(double value, String fieldName) {
        if (!Double.isFinite(value) || value < 0) {
            throw new AppException(fieldName + " must be a finite non-negative number.");
        }
    }

    public static void score(double score, double maxScore) {
        nonNegative(score, "Score");
        nonNegative(maxScore, "Maximum score");
        if (maxScore == 0) throw new AppException("Maximum score cannot be zero.");
        if (score > maxScore) throw new AppException("Score cannot exceed the maximum score.");
    }
}
