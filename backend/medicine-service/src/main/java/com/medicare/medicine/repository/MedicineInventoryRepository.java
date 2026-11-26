package com.medicare.medicine.repository;

import com.medicare.medicine.entity.MedicineInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineInventoryRepository extends JpaRepository<MedicineInventory, Long> {

    Optional<MedicineInventory> findByMedicineId(Long medicineId);

    @Query("SELECT i FROM MedicineInventory i JOIN i.medicine m " +
           "WHERE m.patientId = :patientId AND i.currentStock <= i.minimumThreshold")
    List<MedicineInventory> findLowStockInventories(@Param("patientId") Long patientId);
}
