package com.aiqaos.notification;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * MOD-2: the single governed egress point for outbound comms. Collects every {@link NotificationSender}
 * and routes a {@link Notification} to the sender whose {@link NotificationSender#channel()} matches.
 * A channel with no configured sender yields a failed {@link NotificationResult} — never an exception,
 * so a missing transport can't break a caller.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final Map<NotificationChannel, NotificationSender> byChannel =
            new EnumMap<>(NotificationChannel.class);

    public NotificationService(List<NotificationSender> senders) {
        for (NotificationSender sender : senders) {
            byChannel.put(sender.channel(), sender);
        }
    }

    public NotificationResult notify(Notification notification) {
        if (notification == null || notification.getChannel() == null) {
            return NotificationResult.failed("notification or channel is null");
        }
        NotificationSender sender = byChannel.get(notification.getChannel());
        if (sender == null) {
            log.warn("[Notification] no sender configured for channel {}", notification.getChannel());
            return NotificationResult.failed("no sender configured for channel " + notification.getChannel());
        }
        return sender.send(notification);
    }

    /** Channels that currently have a configured sender. */
    public java.util.Set<NotificationChannel> supportedChannels() {
        return byChannel.keySet();
    }
}
