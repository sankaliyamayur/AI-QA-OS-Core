package com.aiqaos.notification;

/**
 * MOD-2: the outcome of dispatching a notification — whether it was delivered and a message.
 */
public final class NotificationResult {

    private final boolean delivered;
    private final String message;

    public NotificationResult(boolean delivered, String message) {
        this.delivered = delivered;
        this.message = message;
    }

    public static NotificationResult delivered(String message) {
        return new NotificationResult(true, message);
    }

    public static NotificationResult failed(String message) {
        return new NotificationResult(false, message);
    }

    public boolean isDelivered() { return delivered; }
    public String getMessage() { return message; }
}
