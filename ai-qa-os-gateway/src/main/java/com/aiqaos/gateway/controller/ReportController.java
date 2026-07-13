package com.aiqaos.gateway.controller;

import com.aiqaos.gateway.dto.GatewayRequestDTO;
import com.aiqaos.gateway.dto.ReportResponseDTO;
import com.aiqaos.gateway.service.ReportGatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportGatewayService reportGatewayService;

    public ReportController(ReportGatewayService reportGatewayService) {
        this.reportGatewayService = reportGatewayService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ReportResponseDTO> generate(@RequestBody GatewayRequestDTO request) {
        return ResponseEntity.ok(reportGatewayService.generate(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponseDTO> getReport(@PathVariable String id) {
        return ResponseEntity.ok(reportGatewayService.getReport(id));
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<String> export(@PathVariable String id,
                                         @RequestParam(defaultValue = "PDF") String format) {
        return ResponseEntity.ok(reportGatewayService.getExportUrl(id, format));
    }
}