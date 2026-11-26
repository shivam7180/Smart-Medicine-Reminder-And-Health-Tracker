package com.medicare.appointment.repository;

import com.medicare.appointment.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    List<Doctor> findByIsActiveTrue();

    @Query("SELECT d FROM Doctor d WHERE d.isActive = true " +
           "AND (:specializationId IS NULL OR d.specialization.id = :specializationId) " +
           "AND (:keyword IS NULL OR LOWER(d.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Doctor> searchDoctors(
            @Param("specializationId") Long specializationId,
            @Param("keyword") String keyword,
            Pageable pageable);
}
