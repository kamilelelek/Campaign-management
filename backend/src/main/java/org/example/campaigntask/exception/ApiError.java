package org.example.campaigntask.exception;

import java.util.Map;

public record ApiError(String message, Map<String,String> fieldErrors) {
}
