package com.medicare.reminder.dto;

import lombok.Data;

@Data
public class DoseActionRequest {
    private String action; // TAKE, SKIP, SNOOZE
    private String reason; // optional for SKIP
    private Integer snoozeMinutes; // optional for SNOOZE (defaults to 15)
}
