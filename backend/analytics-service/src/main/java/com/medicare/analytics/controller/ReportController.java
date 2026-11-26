package com.medicare.analytics.controller;

import com.medicare.analytics.service.CsvReportService;
import com.medicare.analytics.service.PdfReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Health Reports", description = "PDF and CSV medical summary exports")
public class ReportController {

    private final PdfReportService pdfReportService;
    private final CsvReportService csvReportService;

    @GetMapping("/pdf")
    @Operation(summary = "Download comprehensive patient health & medication adherence PDF report")
    public ResponseEntity<byte[]> downloadPdfReport(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId,
            @RequestParam(defaultValue = "30") int days) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        if (patientId == null) patientId = 1L;

        byte[] pdfBytes = pdfReportService.generatePatientHealthReportPdf(patientId, days);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"medicare_health_report_p" + patientId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/csv")
    @Operation(summary = "Export patient health and adherence history as CSV")
    public ResponseEntity<byte[]> exportCsvReport(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId,
            @RequestParam(defaultValue = "30") int days) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        if (patientId == null) patientId = 1L;

        byte[] csvBytes = csvReportService.generateAdherenceCsv(patientId, days);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"medicare_adherence_export_p" + patientId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
}
