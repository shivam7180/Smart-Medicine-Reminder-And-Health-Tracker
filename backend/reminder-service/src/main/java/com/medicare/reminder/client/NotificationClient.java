package com.medicare.reminder.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notification-service", fallback = NotificationClientFallback.class)
public interface NotificationClient {

    @PostMapping("/api/v1/notifications/internal/send")
    void sendNotification(
            @RequestParam("userId") Long userId,
            @RequestParam("title") String title,
            @RequestParam("message") String message,
            @RequestParam("type") String type,
            @RequestParam(value = "referenceId", required = false) Long referenceId);
}
