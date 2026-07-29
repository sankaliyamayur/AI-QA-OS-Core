package com.aiqaos.notification.event;

import com.aiqaos.notification.NotificationChannel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ENT-2: centralised-notification configuration ({@code aiqaos.notification.*}). {@code defaultChannel}
 * is used when an event does not name its own channel.
 */
@Component
@ConfigurationProperties(prefix = "aiqaos.notification")
public class NotificationEventProperties {

    private NotificationChannel defaultChannel = NotificationChannel.SLACK;

    public NotificationChannel getDefaultChannel() { return defaultChannel; }
    public void setDefaultChannel(NotificationChannel defaultChannel) { this.defaultChannel = defaultChannel; }
}
