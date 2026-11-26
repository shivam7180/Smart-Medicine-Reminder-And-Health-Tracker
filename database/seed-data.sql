-- ============================================================================
-- MediCare Platform: Production-Grade Seed Data
-- Default Demo Password for all accounts: Password@123
-- BCrypt Hash: $2a$10$x8V1vA1dI3R9D8Z5qX9ISeh10w3Qp3NfeJ7CgG7p1O9D2mZ5uHqSu
-- ============================================================================

USE `medicare_auth`;

-- 1. Insert Core Roles
INSERT INTO `roles` (`id`, `name`, `description`) VALUES
(1, 'ROLE_PATIENT', 'Standard Patient user who tracks medications, reminders, and health vitals'),
(2, 'ROLE_DOCTOR', 'Licensed Medical Doctor with patient consultation and prescription privileges'),
(3, 'ROLE_ADMIN', 'Platform Administrator with system oversight and user governance')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- 2. Insert Default Users (Admin, Doctor, Patient)
-- Password for all is 'Password@123'
INSERT INTO `users` (`id`, `email`, `password`, `full_name`, `phone_number`, `date_of_birth`, `gender`, `blood_group`, `emergency_contact_name`, `emergency_contact_phone`, `is_active`) VALUES
(1, 'admin@medicare.com', '$2a$10$x8V1vA1dI3R9D8Z5qX9ISeh10w3Qp3NfeJ7CgG7p1O9D2mZ5uHqSu', 'Dr. System Administrator', '+1-555-0100', '1985-04-12', 'MALE', 'O+', 'Support Desk', '+1-555-0199', TRUE),
(2, 'dr.sarah.connor@medicare.com', '$2a$10$x8V1vA1dI3R9D8Z5qX9ISeh10w3Qp3NfeJ7CgG7p1O9D2mZ5uHqSu', 'Dr. Sarah Connor, MD', '+1-555-0102', '1982-08-20', 'FEMALE', 'A+', 'Hospital Clinic', '+1-555-0198', TRUE),
(3, 'john.doe@medicare.com', '$2a$10$x8V1vA1dI3R9D8Z5qX9ISeh10w3Qp3NfeJ7CgG7p1O9D2mZ5uHqSu', 'Johnathan Doe', '+1-555-0105', '1990-06-15', 'MALE', 'B+', 'Jane Doe', '+1-555-0106', TRUE)
ON DUPLICATE KEY UPDATE `full_name` = VALUES(`full_name`);

-- Assign User Roles
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES
(1, 3), -- Admin
(2, 2), -- Doctor
(3, 1)  -- Patient
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

-- ----------------------------------------------------------------------------
-- 2. MEDICINE SEED DATA
-- ----------------------------------------------------------------------------
USE `medicare_medicine`;

INSERT INTO `medicine_categories` (`id`, `name`, `description`) VALUES
(1, 'Antidiabetic', 'Medications to manage and regulate blood glucose levels'),
(2, 'Antihypertensive', 'Medications used to treat hypertension (high blood pressure)'),
(3, 'Analgesics & Antipyretics', 'Pain relievers and fever reducers'),
(4, 'Antibiotics', 'Antimicrobial drugs for treating bacterial infections'),
(5, 'Cardiovascular & Statins', 'Medications for cholesterol and cardiovascular support'),
(6, 'Vitamins & Supplements', 'Daily essential dietary supplements and micronutrients')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- Seed Medicines for John Doe (patient_id = 3)
INSERT INTO `medicines` (`id`, `patient_id`, `category_id`, `name`, `generic_name`, `medicine_type`, `strength`, `dosage_instructions`, `before_after_food`, `start_date`, `end_date`, `prescribing_doctor`, `notes`, `is_active`) VALUES
(1, 3, 1, 'Metformin Extended Release', 'Metformin HCl', 'TABLET', '500 mg', 'Take with evening meal to prevent stomach upset', 'WITH_FOOD', '2026-01-01', '2026-12-31', 'Dr. Sarah Connor', 'Keep track of fasting glucose', TRUE),
(2, 3, 2, 'Lisinopril', 'Lisinopril Dihydrate', 'TABLET', '10 mg', 'Take once daily in the morning', 'BEFORE_FOOD', '2026-01-01', '2026-12-31', 'Dr. Sarah Connor', 'Monitor morning blood pressure', TRUE),
(3, 3, 3, 'Paracetamol', 'Acetaminophen', 'TABLET', '650 mg', 'Take as needed for fever or headache; max 3 daily', 'AFTER_FOOD', '2026-05-01', '2026-06-01', 'Dr. Sarah Connor', 'Do not exceed 3000mg per day', TRUE),
(4, 3, 6, 'Omega-3 Fish Oil', 'EPA / DHA Fatty Acids', 'CAPSULE', '1000 mg', 'Take one capsule with breakfast', 'AFTER_FOOD', '2026-01-01', NULL, 'Dr. Sarah Connor', 'Heart health supplement', TRUE)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- Seed Inventory for John Doe's Medicines
INSERT INTO `medicine_inventory` (`medicine_id`, `current_stock`, `minimum_threshold`, `reorder_quantity`, `expiry_date`, `batch_number`, `unit_price`) VALUES
(1, 45, 10, 60, '2027-08-31', 'MET-2024-09A', 12.50),
(2, 8, 10, 30, '2027-11-15', 'LIS-2024-03B', 18.00), -- Low stock alert triggered!
(3, 20, 5, 20, '2026-12-31', 'PCM-2024-11C', 6.00),
(4, 55, 15, 60, '2028-01-01', 'OMG-2024-01A', 22.00)
ON DUPLICATE KEY UPDATE `current_stock` = VALUES(`current_stock`);

-- ----------------------------------------------------------------------------
-- 3. REMINDER SEED DATA
-- ----------------------------------------------------------------------------
USE `medicare_reminder`;

-- Schedules for John Doe (patient_id = 3)
INSERT INTO `medicine_schedules` (`id`, `patient_id`, `medicine_id`, `medicine_name`, `dosage_quantity`, `frequency_type`, `start_date`, `end_date`, `is_active`, `time_zone`) VALUES
(1, 3, 1, 'Metformin Extended Release', '1 Tablet (500mg)', 'DAILY', '2026-01-01', '2026-12-31', TRUE, 'UTC'),
(2, 3, 2, 'Lisinopril', '1 Tablet (10mg)', 'DAILY', '2026-01-01', '2026-12-31', TRUE, 'UTC'),
(3, 3, 4, 'Omega-3 Fish Oil', '1 Capsule (1000mg)', 'DAILY', '2026-01-01', NULL, TRUE, 'UTC')
ON DUPLICATE KEY UPDATE `medicine_name` = VALUES(`medicine_name`);

-- Schedule Reminder Times
INSERT INTO `schedule_times` (`schedule_id`, `reminder_time`) VALUES
(1, '20:00:00'), -- Evening 8:00 PM for Metformin
(2, '08:00:00'), -- Morning 8:00 AM for Lisinopril
(3, '08:00:00')  -- Morning 8:00 AM for Omega-3
ON DUPLICATE KEY UPDATE `reminder_time` = VALUES(`reminder_time`);

-- Seed Recent Dose Logs for today and yesterday
INSERT INTO `dose_logs` (`id`, `schedule_id`, `patient_id`, `medicine_id`, `medicine_name`, `scheduled_time`, `actual_time`, `status`, `skip_reason`, `snooze_count`) VALUES
(1, 2, 3, 2, 'Lisinopril', '2026-11-25 08:00:00', '2026-11-25 08:05:12', 'TAKEN', NULL, 0),
(2, 3, 3, 'Omega-3 Fish Oil', '2026-11-25 08:00:00', '2026-11-25 08:06:00', 'TAKEN', NULL, 0),
(3, 1, 3, 'Metformin Extended Release', '2026-11-25 20:00:00', NULL, 'SCHEDULED', NULL, 0),
(4, 2, 3, 'Lisinopril', '2026-11-24 08:00:00', '2026-11-24 08:12:00', 'TAKEN', NULL, 0),
(5, 1, 3, 'Metformin Extended Release', '2026-11-24 20:00:00', '2026-11-24 20:25:00', 'TAKEN', NULL, 0),
(6, 2, 3, 'Lisinopril', '2026-11-23 08:00:00', NULL, 'MISSED', 'Forgot before leaving for commute', 0),
(7, 1, 3, 'Metformin Extended Release', '2026-11-23 20:00:00', '2026-11-23 20:10:00', 'TAKEN', NULL, 0)
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

-- ----------------------------------------------------------------------------
-- 4. HEALTH TRACKING SEED DATA
-- ----------------------------------------------------------------------------
USE `medicare_health`;

INSERT INTO `health_records` (`patient_id`, `record_type`, `value_primary`, `value_secondary`, `unit`, `reading_time`, `notes`, `abnormal_flag`) VALUES
(3, 'BLOOD_PRESSURE', 120.00, 80.00, 'mmHg', '2026-11-25 08:30:00', 'Normal resting morning measurement', FALSE),
(3, 'BLOOD_SUGAR', 98.00, NULL, 'mg/dL', '2026-11-25 07:45:00', 'Fasting blood glucose', FALSE),
(3, 'HEART_RATE', 72.00, NULL, 'bpm', '2026-11-25 08:30:00', 'Resting pulse rate', FALSE),
(3, 'WEIGHT', 74.50, NULL, 'kg', '2026-11-25 07:30:00', 'Morning weight after waking', FALSE),
(3, 'HEIGHT', 178.00, NULL, 'cm', '2026-11-01 10:00:00', 'Baseline clinic measurement', FALSE),
(3, 'SPO2', 99.00, NULL, '%', '2026-11-25 08:30:00', 'Oxygen saturation room air', FALSE),
(3, 'WATER_INTAKE', 2250.00, NULL, 'ml', '2026-11-24 21:00:00', 'Total daily hydration logged', FALSE),
(3, 'SLEEP', 7.50, NULL, 'hours', '2026-11-25 07:00:00', 'Restful sleep duration', FALSE),
-- Historical readings for weekly trends
(3, 'BLOOD_PRESSURE', 122.00, 82.00, 'mmHg', '2026-11-24 08:30:00', 'Morning reading', FALSE),
(3, 'BLOOD_PRESSURE', 128.00, 85.00, 'mmHg', '2026-11-23 08:30:00', 'Slightly elevated after stressful day', TRUE),
(3, 'BLOOD_SUGAR', 104.00, NULL, 'mg/dL', '2026-11-24 07:45:00', 'Fasting glucose', FALSE),
(3, 'BLOOD_SUGAR', 112.00, NULL, 'mg/dL', '2026-11-23 07:45:00', 'Fasting glucose', TRUE);

-- Health Goals for John Doe
INSERT INTO `health_goals` (`patient_id`, `goal_type`, `target_value`, `current_value`, `unit`, `start_date`, `target_date`, `status`) VALUES
(3, 'WEIGHT', 72.00, 74.50, 'kg', '2026-11-01', '2026-12-31', 'IN_PROGRESS'),
(3, 'WATER_INTAKE', 2500.00, 2250.00, 'ml', '2026-11-01', NULL, 'IN_PROGRESS'),
(3, 'BLOOD_PRESSURE_SYSTOLIC', 120.00, 120.00, 'mmHg', '2026-11-01', NULL, 'ACHIEVED')
ON DUPLICATE KEY UPDATE `current_value` = VALUES(`current_value`);

-- ----------------------------------------------------------------------------
-- 5. NOTIFICATION SEED DATA
-- ----------------------------------------------------------------------------
USE `medicare_notification`;

INSERT INTO `notification_preferences` (`user_id`, `email_enabled`, `in_app_enabled`, `reminder_lead_minutes`, `low_stock_alerts`) VALUES
(3, TRUE, TRUE, 15, TRUE)
ON DUPLICATE KEY UPDATE `email_enabled` = VALUES(`email_enabled`);

INSERT INTO `notifications` (`user_id`, `title`, `message`, `notification_type`, `reference_id`, `is_read`, `created_at`) VALUES
(3, 'Low Stock Warning: Lisinopril', 'You have only 8 tablets of Lisinopril remaining (minimum threshold: 10). Please request a refill.', 'LOW_STOCK', 2, FALSE, '2026-11-25 08:10:00'),
(3, 'Upcoming Dose Reminder', 'Reminder: Metformin Extended Release (500mg) is scheduled today at 8:00 PM with dinner.', 'MEDICINE_REMINDER', 1, FALSE, '2026-11-25 18:00:00'),
(3, 'Dose Confirmed', 'You successfully logged Lisinopril (10mg) at 8:05 AM. Keep up the high adherence!', 'MEDICINE_REMINDER', 2, TRUE, '2026-11-25 08:05:12')
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`);

-- ----------------------------------------------------------------------------
-- 6. DOCTOR & APPOINTMENT SEED DATA
-- ----------------------------------------------------------------------------
USE `medicare_appointment`;

INSERT INTO `doctor_specializations` (`id`, `name`, `description`) VALUES
(1, 'Cardiology', 'Heart health, vascular disease, and blood pressure control'),
(2, 'Endocrinology', 'Diabetes, metabolic disorders, and thyroid care'),
(3, 'General Internal Medicine', 'Primary care, preventive health, and acute illness management'),
(4, 'Neurology', 'Nervous system, migraines, and cognitive wellness')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

INSERT INTO `doctors` (`id`, `user_id`, `full_name`, `email`, `phone_number`, `specialization_id`, `qualification`, `experience_years`, `consultation_fee`, `bio`, `available_days`) VALUES
(1, 2, 'Dr. Sarah Connor, MD', 'dr.sarah.connor@medicare.com', '+1-555-0102', 1, 'MD - Cardiology (Johns Hopkins University)', 12, 120.00, 'Board-certified cardiologist specializing in preventive cardiology, hypertension treatment, and lifestyle interventions.', 'MON,TUE,WED,THU,FRI')
ON DUPLICATE KEY UPDATE `full_name` = VALUES(`full_name`);

INSERT INTO `appointments` (`id`, `patient_id`, `doctor_id`, `appointment_date`, `appointment_time`, `reason`, `status`, `doctor_notes`) VALUES
(1, 3, 1, '2026-12-05', '10:30:00', 'Quarterly routine hypertension and metformin tolerance review', 'CONFIRMED', 'Please bring recent 14-day blood pressure logs.')
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);
