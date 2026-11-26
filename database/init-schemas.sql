-- ============================================================================
-- MediCare Platform: Multi-Schema Database Initialization Script (MySQL 8)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `medicare_auth` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `medicare_medicine` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `medicare_reminder` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `medicare_health` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `medicare_notification` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `medicare_appointment` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `medicare_analytics` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `medicare_admin` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 1. AUTH & USER SERVICE SCHEMA (medicare_auth)
-- ----------------------------------------------------------------------------
USE `medicare_auth`;

CREATE TABLE IF NOT EXISTS `roles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(150) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(120) NOT NULL,
    `phone_number` VARCHAR(25) NULL,
    `date_of_birth` DATE NULL,
    `gender` VARCHAR(20) NULL,
    `blood_group` VARCHAR(10) NULL,
    `address` TEXT NULL,
    `emergency_contact_name` VARCHAR(120) NULL,
    `emergency_contact_phone` VARCHAR(25) NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_users_email` (`email`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `user_roles` (
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    CONSTRAINT `fk_ur_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ur_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `refresh_tokens` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `token` VARCHAR(512) NOT NULL UNIQUE,
    `expiry_date` TIMESTAMP NOT NULL,
    `revoked` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_rt_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    INDEX `idx_rt_token` (`token`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `password_reset_tokens` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `token` VARCHAR(255) NOT NULL UNIQUE,
    `expiry_date` TIMESTAMP NOT NULL,
    `used` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_prt_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 2. MEDICINE SERVICE SCHEMA (medicare_medicine)
-- ----------------------------------------------------------------------------
USE `medicare_medicine`;

CREATE TABLE IF NOT EXISTS `medicine_categories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `medicines` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `patient_id` BIGINT NOT NULL,
    `category_id` BIGINT NULL,
    `name` VARCHAR(150) NOT NULL,
    `generic_name` VARCHAR(150) NULL,
    `medicine_type` VARCHAR(50) NOT NULL,
    `strength` VARCHAR(50) NOT NULL,
    `dosage_instructions` VARCHAR(255) NULL,
    `before_after_food` VARCHAR(30) DEFAULT 'AFTER_FOOD',
    `start_date` DATE NOT NULL,
    `end_date` DATE NULL,
    `prescribing_doctor` VARCHAR(150) NULL,
    `notes` TEXT NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_med_category` FOREIGN KEY (`category_id`) REFERENCES `medicine_categories` (`id`) ON DELETE SET NULL,
    INDEX `idx_med_patient` (`patient_id`),
    INDEX `idx_med_name` (`name`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `medicine_inventory` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `medicine_id` BIGINT NOT NULL UNIQUE,
    `current_stock` INT NOT NULL DEFAULT 0,
    `minimum_threshold` INT NOT NULL DEFAULT 5,
    `reorder_quantity` INT NOT NULL DEFAULT 20,
    `expiry_date` DATE NULL,
    `batch_number` VARCHAR(50) NULL,
    `unit_price` DECIMAL(10,2) DEFAULT 0.00,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_inv_medicine` FOREIGN KEY (`medicine_id`) REFERENCES `medicines` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `prescriptions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `patient_id` BIGINT NOT NULL,
    `doctor_id` BIGINT NULL,
    `doctor_name` VARCHAR(150) NOT NULL,
    `prescription_date` DATE NOT NULL,
    `diagnosis` VARCHAR(255) NULL,
    `notes` TEXT NULL,
    `prescription_file_path` VARCHAR(500) NULL,
    `file_name` VARCHAR(255) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_presc_patient` (`patient_id`)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 3. REMINDER SERVICE SCHEMA (medicare_reminder)
-- ----------------------------------------------------------------------------
USE `medicare_reminder`;

CREATE TABLE IF NOT EXISTS `medicine_schedules` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `patient_id` BIGINT NOT NULL,
    `medicine_id` BIGINT NOT NULL,
    `medicine_name` VARCHAR(150) NOT NULL,
    `dosage_quantity` VARCHAR(50) NOT NULL DEFAULT '1',
    `frequency_type` VARCHAR(50) NOT NULL DEFAULT 'DAILY',
    `start_date` DATE NOT NULL,
    `end_date` DATE NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `time_zone` VARCHAR(50) DEFAULT 'UTC',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_sched_patient` (`patient_id`),
    INDEX `idx_sched_medicine` (`medicine_id`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `schedule_times` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `schedule_id` BIGINT NOT NULL,
    `reminder_time` TIME NOT NULL,
    CONSTRAINT `fk_st_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `medicine_schedules` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_sched_time` (`schedule_id`, `reminder_time`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `dose_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `schedule_id` BIGINT NOT NULL,
    `patient_id` BIGINT NOT NULL,
    `medicine_id` BIGINT NOT NULL,
    `medicine_name` VARCHAR(150) NOT NULL,
    `scheduled_time` DATETIME NOT NULL,
    `actual_time` DATETIME NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    `skip_reason` VARCHAR(255) NULL,
    `snooze_count` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_dose_patient_time` (`patient_id`, `scheduled_time`),
    INDEX `idx_dose_status` (`status`)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 4. HEALTH TRACKING SCHEMA (medicare_health)
-- ----------------------------------------------------------------------------
USE `medicare_health`;

CREATE TABLE IF NOT EXISTS `health_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `patient_id` BIGINT NOT NULL,
    `record_type` VARCHAR(50) NOT NULL,
    `value_primary` DECIMAL(10,2) NOT NULL,
    `value_secondary` DECIMAL(10,2) NULL,
    `unit` VARCHAR(30) NOT NULL,
    `reading_time` DATETIME NOT NULL,
    `notes` TEXT NULL,
    `abnormal_flag` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_hr_patient_type` (`patient_id`, `record_type`),
    INDEX `idx_hr_reading_time` (`reading_time`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `health_goals` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `patient_id` BIGINT NOT NULL,
    `goal_type` VARCHAR(50) NOT NULL,
    `target_value` DECIMAL(10,2) NOT NULL,
    `current_value` DECIMAL(10,2) DEFAULT 0.00,
    `unit` VARCHAR(30) NOT NULL,
    `start_date` DATE NOT NULL,
    `target_date` DATE NULL,
    `status` VARCHAR(30) DEFAULT 'IN_PROGRESS',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_goal_patient` (`patient_id`)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 5. NOTIFICATION SERVICE SCHEMA (medicare_notification)
-- ----------------------------------------------------------------------------
USE `medicare_notification`;

CREATE TABLE IF NOT EXISTS `notifications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `message` TEXT NOT NULL,
    `notification_type` VARCHAR(50) NOT NULL,
    `reference_id` BIGINT NULL,
    `is_read` BOOLEAN NOT NULL DEFAULT FALSE,
    `read_at` DATETIME NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_notif_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `notification_preferences` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL UNIQUE,
    `email_enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `in_app_enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `reminder_lead_minutes` INT NOT NULL DEFAULT 15,
    `low_stock_alerts` BOOLEAN NOT NULL DEFAULT TRUE,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `notification_delivery_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `notification_id` BIGINT NOT NULL,
    `channel` VARCHAR(30) NOT NULL,
    `status` VARCHAR(30) NOT NULL,
    `error_message` TEXT NULL,
    `attempt_count` INT NOT NULL DEFAULT 1,
    `delivered_at` DATETIME NULL,
    INDEX `idx_ndl_notif` (`notification_id`)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 6. DOCTOR & APPOINTMENT SCHEMA (medicare_appointment)
-- ----------------------------------------------------------------------------
USE `medicare_appointment`;

CREATE TABLE IF NOT EXISTS `doctor_specializations` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `doctors` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL UNIQUE,
    `full_name` VARCHAR(120) NOT NULL,
    `email` VARCHAR(150) NOT NULL UNIQUE,
    `phone_number` VARCHAR(25) NULL,
    `specialization_id` BIGINT NULL,
    `qualification` VARCHAR(100) NOT NULL,
    `experience_years` INT NOT NULL DEFAULT 1,
    `consultation_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `bio` TEXT NULL,
    `available_days` VARCHAR(100) DEFAULT 'MON,TUE,WED,THU,FRI',
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_doc_spec` FOREIGN KEY (`specialization_id`) REFERENCES `doctor_specializations` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `appointments` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `patient_id` BIGINT NOT NULL,
    `doctor_id` BIGINT NOT NULL,
    `appointment_date` DATE NOT NULL,
    `appointment_time` TIME NOT NULL,
    `reason` VARCHAR(255) NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    `doctor_notes` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_appt_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`id`) ON DELETE CASCADE,
    INDEX `idx_appt_patient` (`patient_id`),
    INDEX `idx_appt_date` (`appointment_date`)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 7. ANALYTICS SCHEMA (medicare_analytics)
-- ----------------------------------------------------------------------------
USE `medicare_analytics`;

CREATE TABLE IF NOT EXISTS `adherence_snapshots` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `patient_id` BIGINT NOT NULL,
    `snapshot_date` DATE NOT NULL,
    `total_scheduled` INT NOT NULL DEFAULT 0,
    `taken_count` INT NOT NULL DEFAULT 0,
    `skipped_count` INT NOT NULL DEFAULT 0,
    `missed_count` INT NOT NULL DEFAULT 0,
    `adherence_percentage` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_adh_patient_date` (`patient_id`, `snapshot_date`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `health_reports` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `patient_id` BIGINT NOT NULL,
    `report_title` VARCHAR(200) NOT NULL,
    `report_type` VARCHAR(50) NOT NULL,
    `start_date` DATE NOT NULL,
    `end_date` DATE NOT NULL,
    `adherence_rate` DECIMAL(5,2) NOT NULL,
    `vitals_summary_json` JSON NULL,
    `generated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_hr_patient` (`patient_id`)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 8. ADMIN SCHEMA (medicare_admin)
-- ----------------------------------------------------------------------------
USE `medicare_admin`;

CREATE TABLE IF NOT EXISTS `audit_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NULL,
    `user_email` VARCHAR(150) NULL,
    `action` VARCHAR(100) NOT NULL,
    `entity_name` VARCHAR(100) NOT NULL,
    `entity_id` VARCHAR(100) NULL,
    `ip_address` VARCHAR(50) NULL,
    `details` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_audit_email` (`user_email`),
    INDEX `idx_audit_created` (`created_at`)
) ENGINE=InnoDB;
