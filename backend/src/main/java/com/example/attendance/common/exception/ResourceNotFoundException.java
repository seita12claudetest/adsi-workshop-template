package com.example.attendance.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super(resourceName + " が見つかりません (ID: " + id + ")");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
