package com.medicare.medicine.repository;

import com.medicare.medicine.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByPatientIdAndIsActiveTrue(Long patientId);

    Page<Medicine> findByPatientId(Long patientId, Pageable pageable);

    Optional<Medicine> findByIdAndPatientId(Long id, Long patientId);

    @Query("SELECT m FROM Medicine m WHERE m.patientId = :patientId " +
           "AND (:keyword IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryId IS NULL OR m.category.id = :categoryId) " +
           "AND (:isActive IS NULL OR m.isActive = :isActive)")
    Page<Medicine> searchMedicines(
            @Param("patientId") Long patientId,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    long countByPatientIdAndIsActiveTrue(Long patientId);

    @Query("SELECT m FROM Medicine m JOIN m.inventory i WHERE m.patientId = :patientId AND i.expiryDate <= :expiryThreshold")
    List<Medicine> findExpiringMedicines(@Param("patientId") Long patientId, @Param("expiryThreshold") LocalDate expiryThreshold);
}
