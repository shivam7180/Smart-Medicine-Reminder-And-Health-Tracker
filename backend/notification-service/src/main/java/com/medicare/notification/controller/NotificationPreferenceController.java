package com.medicare.notification.controller;

import com.medicare.common.dto.ApiResponse;
import com.medicare.notification.dto.NotificationPreferenceDto;
import com.medicare.notification.service.NotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notification-preferences")
@RequiredArgsConstructor
@Tag(name = "Notification Preferences", description = "Manage alert channels (email, in-app, low-stock notifications)")
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @GetMapping
    @Operation(summary = "Get user notification preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceDto>> getPreferences(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId) {
        Long userId = paramUserId != null ? paramUserId : headerUserId;
        if (userId == null) userId = 1L;
        return ResponseEntity.ok(ApiResponse.success(preferenceService.getPreferences(userId)));
    }

    @PutMapping
    @Operation(summary = "Update user notification preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceDto>> updatePreferences(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @RequestBody NotificationPreferenceDto dto) {
        Long userId = paramUserId != null ? paramUserId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(preferenceService.updatePreferences(userId, dto), "Preferences updated successfully"));
    }
}
