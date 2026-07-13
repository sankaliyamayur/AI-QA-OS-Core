package com.aiqaos.observability.alert;

public interface NotificationAdapter {
    void sendNotification(String alertId, String message);
}