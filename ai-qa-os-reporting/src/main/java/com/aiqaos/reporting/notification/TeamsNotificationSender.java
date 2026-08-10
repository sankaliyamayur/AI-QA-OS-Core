package com.aiqaos.reporting.notification;

import org.springframework.stereotype.Component;

/**
 * Legacy placeholder — see {@link SlackNotificationSender}. Named explicitly so it does not collide
 * with MOD-2's {@code com.aiqaos.notification.TeamsNotificationSender} during component scan.
 */
@Component("reportingTeamsNotificationSender")
public class TeamsNotificationSender {
}