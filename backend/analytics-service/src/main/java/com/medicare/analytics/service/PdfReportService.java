package com.medicare.analytics.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.medicare.analytics.client.HealthFeignClient;
import com.medicare.analytics.client.MedicineFeignClient;
import com.medicare.analytics.client.ReminderFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfReportService {

    private final MedicineFeignClient medicineClient;
    private final ReminderFeignClient reminderClient;
    private final HealthFeignClient healthClient;

    public byte[] generatePatientHealthReportPdf(Long patientId, int days) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(14, 116, 144)); // Teal/Blue
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(30, 41, 59));
            Font boldCellFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            // 1. Header Banner
            Paragraph title = new Paragraph("MediCare Comprehensive Health & Adherence Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph meta = new Paragraph("Patient ID: #" + patientId + "  |  Period: Past " + days + " Days  |  Generated: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), subTitleFont);
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingAfter(20);
            document.add(meta);

            // 2. Medicine Adherence Summary Table
            Paragraph adhHeading = new Paragraph("1. Medication Adherence Summary", sectionFont);
            adhHeading.setSpacingAfter(8);
            document.add(adhHeading);

            PdfPTable adhTable = new PdfPTable(5);
            adhTable.setWidthPercentage(100);
            adhTable.setWidths(new float[]{2f, 2f, 2f, 2f, 2.5f});

            addTableHeader(adhTable, new String[]{"Scheduled", "Taken", "Skipped", "Missed", "Adherence Rate"}, boldCellFont);

            Map<String, Object> adhData = reminderClient.getAdherence(patientId, days).getData();
            if (adhData != null) {
                adhTable.addCell(createCell(String.valueOf(adhData.getOrDefault("totalScheduled", "0")), regularFont));
                adhTable.addCell(createCell(String.valueOf(adhData.getOrDefault("takenCount", "0")), regularFont));
                adhTable.addCell(createCell(String.valueOf(adhData.getOrDefault("skippedCount", "0")), regularFont));
                adhTable.addCell(createCell(String.valueOf(adhData.getOrDefault("missedCount", "0")), regularFont));
                adhTable.addCell(createCell(adhData.getOrDefault("adherencePercentage", "0") + "%", regularFont));
            }
            adhTable.setSpacingAfter(20);
            document.add(adhTable);

            // 3. Active Medicines List
            Paragraph medHeading = new Paragraph("2. Prescribed Medications Catalog", sectionFont);
            medHeading.setSpacingAfter(8);
            document.add(medHeading);

            PdfPTable medTable = new PdfPTable(4);
            medTable.setWidthPercentage(100);
            medTable.setWidths(new float[]{3f, 2f, 2f, 3f});
            addTableHeader(medTable, new String[]{"Medicine Name", "Dosage", "Type", "Instructions"}, boldCellFont);

            List<Map<String, Object>> medicines = medicineClient.getActiveMedicines(patientId).getData();
            if (medicines != null && !medicines.isEmpty()) {
                for (Map<String, Object> m : medicines) {
                    medTable.addCell(createCell(String.valueOf(m.getOrDefault("name", "N/A")), regularFont));
                    medTable.addCell(createCell(String.valueOf(m.getOrDefault("strength", "N/A")), regularFont));
                    medTable.addCell(createCell(String.valueOf(m.getOrDefault("medicineType", "N/A")), regularFont));
                    medTable.addCell(createCell(String.valueOf(m.getOrDefault("beforeAfterFood", "N/A")), regularFont));
                }
            } else {
                PdfPCell empty = new PdfPCell(new Phrase("No active medications found", regularFont));
                empty.setColspan(4);
                empty.setHorizontalAlignment(Element.ALIGN_CENTER);
                medTable.addCell(empty);
            }
            medTable.setSpacingAfter(20);
            document.add(medTable);

            // 4. Latest Health Vitals
            Paragraph vitalsHeading = new Paragraph("3. Latest Biometric Health Readings", sectionFont);
            vitalsHeading.setSpacingAfter(8);
            document.add(vitalsHeading);

            PdfPTable vitalsTable = new PdfPTable(4);
            vitalsTable.setWidthPercentage(100);
            vitalsTable.setWidths(new float[]{2.5f, 2.5f, 2.5f, 2.5f});
            addTableHeader(vitalsTable, new String[]{"Vital Sign", "Measurement", "Vital Sign", "Measurement"}, boldCellFont);

            Map<String, Object> vitals = healthClient.getLatestVitals(patientId).getData();
            if (vitals != null) {
                String bp = vitals.get("bloodPressureSystolic") != null
                        ? vitals.get("bloodPressureSystolic") + "/" + vitals.get("bloodPressureDiastolic") + " mmHg"
                        : "N/A";
                vitalsTable.addCell(createCell("Blood Pressure", regularFont));
                vitalsTable.addCell(createCell(bp, regularFont));

                String sugar = vitals.get("bloodSugar") != null ? vitals.get("bloodSugar") + " mg/dL" : "N/A";
                vitalsTable.addCell(createCell("Blood Sugar", regularFont));
                vitalsTable.addCell(createCell(sugar, regularFont));

                String hr = vitals.get("heartRate") != null ? vitals.get("heartRate") + " bpm" : "N/A";
                vitalsTable.addCell(createCell("Heart Rate", regularFont));
                vitalsTable.addCell(createCell(hr, regularFont));

                String bmi = vitals.get("bmi") != null ? vitals.get("bmi") + " (" + vitals.get("bmiCategory") + ")" : "N/A";
                vitalsTable.addCell(createCell("BMI", regularFont));
                vitalsTable.addCell(createCell(bmi, regularFont));
            }
            vitalsTable.setSpacingAfter(25);
            document.add(vitalsTable);

            // Disclaimer
            Paragraph disclaimer = new Paragraph("Notice: This clinical summary report is automatically generated for personal health adherence tracking and reference. It does not replace professional medical diagnosis or consultation.", subTitleFont);
            disclaimer.setAlignment(Element.ALIGN_CENTER);
            document.add(disclaimer);

            document.close();
            log.info("Generated PDF report for patient {} ({} bytes)", patientId, out.size());
        } catch (Exception e) {
            log.error("Failed to generate PDF report: {}", e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String[] headers, Font font) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, font));
            cell.setBackgroundColor(new Color(14, 116, 144));
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
}
