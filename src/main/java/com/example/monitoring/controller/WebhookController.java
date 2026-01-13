package com.example.monitoring.controller;

import com.example.monitoring.dto.DatadogAlertRequestDTO;
import com.example.monitoring.service.AlertHandlerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private final AlertHandlerService alertHandlerService;

    public WebhookController(AlertHandlerService alertHandlerService) {
        this.alertHandlerService = alertHandlerService;
    }

    @PostMapping("/alerts/datadog")
    public ResponseEntity<Void> handleDatadogAlert(@Valid @RequestBody DatadogAlertRequestDTO dto) {
        alertHandlerService.handleDatadogAlert(dto);
        return ResponseEntity.noContent().build();
    }
}
