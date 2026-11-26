package com.medicare.appointment.repository;

import com.medicare.appointment.entity.DoctorSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorSpecializationRepository extends JpaRepository<DoctorSpecialization, Long> {
    Optional<DoctorSpecialization> findByName(String name);
}
