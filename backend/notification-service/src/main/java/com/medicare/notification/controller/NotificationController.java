package com.medicare.notification.controller;

import com.medicare.common.dto.ApiResponse;
import com.medicare.notification.dto.CreateNotificationRequest;
import com.medicare.notification.dto.NotificationDto;
import com.medicare.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app alerts, badge counters, and notification feed")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get paginated notification history for user")
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getNotifications(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Long userId = paramUserId != null ? paramUserId : headerUserId;
        if (userId == null) userId = 1L;
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUserNotifications(userId, PageRequest.of(page, size))));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get list of all unread notifications")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getUnread(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId) {
        Long userId = paramUserId != null ? paramUserId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadNotifications(userId)));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get badge count of unread notifications")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId) {
        Long userId = paramUserId != null ? paramUserId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount(userId)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark single notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId) {
        Long userId = paramUserId != null ? paramUserId : headerUserId;
        notificationService.markAsRead(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId) {
        Long userId = paramUserId != null ? paramUserId : headerUserId;
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId) {
        Long userId = paramUserId != null ? paramUserId : headerUserId;
        notificationService.deleteNotification(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted"));
    }

    @PostMapping("/internal/send")
    @Operation(summary = "Internal microservice endpoint: Dispatch notification")
    public ResponseEntity<Void> sendInternal(
            @RequestParam("userId") Long userId,
            @RequestParam("title") String title,
            @RequestParam("message") String message,
            @RequestParam("type") String type,
            @RequestParam(value = "referenceId", required = false) Long referenceId) {
        notificationService.createNotification(CreateNotificationRequest.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .notificationType(type)
                .referenceId(referenceId)
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
