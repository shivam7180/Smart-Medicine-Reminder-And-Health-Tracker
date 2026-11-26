package com.medicare.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_delivery_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(nullable = false, length = 30)
    private String channel; // EMAIL, IN_APP

    @Column(nullable = false, length = 30)
    private String status; // SUCCESS, FAILED, RETRYING

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Builder.Default
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 1;

    @CreationTimestamp
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
}
