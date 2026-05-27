package com.auction.models.dto;

/**
 * A reusable response for various network operations.
 */
public class GenericResponse implements NetworkMessage {
    private final boolean success;
    private final String message;
    private final Object data;

    public GenericResponse(boolean success, String message) {
        this(success, message, null);
    }

    public GenericResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "GenericResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
