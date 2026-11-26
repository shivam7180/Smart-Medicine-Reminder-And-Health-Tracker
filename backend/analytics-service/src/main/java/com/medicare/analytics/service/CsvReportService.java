package com.medicare.analytics.service;

import com.medicare.analytics.client.MedicineFeignClient;
import com.medicare.analytics.client.ReminderFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvReportService {

    private final MedicineFeignClient medicineClient;
    private final ReminderFeignClient reminderClient;

    public byte[] generateAdherenceCsv(Long patientId, int days) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8), CSVFormat.DEFAULT)) {
            // Header
            printer.printRecord("Patient ID", "Metric", "Value");

            Map<String, Object> adh = reminderClient.getAdherence(patientId, days).getData();
            if (adh != null) {
                printer.printRecord(patientId, "Total Scheduled Doses", adh.getOrDefault("totalScheduled", 0));
                printer.printRecord(patientId, "Taken Doses", adh.getOrDefault("takenCount", 0));
                printer.printRecord(patientId, "Skipped Doses", adh.getOrDefault("skippedCount", 0));
                printer.printRecord(patientId, "Missed Doses", adh.getOrDefault("missedCount", 0));
                printer.printRecord(patientId, "Adherence Percentage", adh.getOrDefault("adherencePercentage", "0") + "%");
            }

            // Medicines
            printer.println();
            printer.printRecord("Medicine Name", "Strength", "Type", "Instructions", "Current Stock");
            List<Map<String, Object>> medicines = medicineClient.getActiveMedicines(patientId).getData();
            if (medicines != null) {
                for (Map<String, Object> m : medicines) {
                    printer.printRecord(
                            m.getOrDefault("name", ""),
                            m.getOrDefault("strength", ""),
                            m.getOrDefault("medicineType", ""),
                            m.getOrDefault("beforeAfterFood", ""),
                            m.getOrDefault("currentStock", "")
                    );
                }
            }
            printer.flush();
        } catch (Exception e) {
            log.error("Failed to generate CSV export: {}", e.getMessage(), e);
        }

        return out.toByteArray();
    }
}
