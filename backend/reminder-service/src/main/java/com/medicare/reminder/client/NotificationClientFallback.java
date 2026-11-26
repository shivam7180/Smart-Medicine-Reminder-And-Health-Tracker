package com.medicare.reminder.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationClientFallback implements NotificationClient {

    @Override
    public void sendNotification(Long userId, String title, String message, String type, Long referenceId) {
        log.warn("Fallback triggered: Unable to deliver notification to user {} - notification-service unavailable", userId);
    }
}
