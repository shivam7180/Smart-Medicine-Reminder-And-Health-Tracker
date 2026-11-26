package com.medicare.reminder.repository;

import com.medicare.reminder.entity.ReminderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReminderEventRepository extends JpaRepository<ReminderEvent, Long> {
    List<ReminderEvent> findByPatientIdOrderByGeneratedAtDesc(Long patientId);
}
