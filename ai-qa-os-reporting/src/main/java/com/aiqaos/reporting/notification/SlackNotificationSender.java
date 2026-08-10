package com.aiqaos.reporting.notification;

import org.springframework.stereotype.Component;

/**
 * Legacy placeholder. The real Slack sender is MOD-2's {@code com.aiqaos.notification
 * .SlackNotificationSender}; this empty stub shares its simple name, so under the apps'
 * {@code com.aiqaos} component scan both claim the default bean name {@code slackNotificationSender}
 * and the context fails to start. Named explicitly so the two can coexist.
 */
@Component("reportingSlackNotificationSender")
public class SlackNotificationSender {
}