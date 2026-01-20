package nu.ndw.realtime.monitoring.controller;

import nu.ndw.realtime.monitoring.dto.DatadogAlertRequestDTO;
import nu.ndw.realtime.monitoring.dto.GrafanaAlertRequestDTO;
import nu.ndw.realtime.monitoring.service.AlertHandlerService;
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

    @PostMapping("/alerts/grafana")
    public ResponseEntity<Void> handleGrafanaAlert(@RequestBody GrafanaAlertRequestDTO dto) {
        alertHandlerService.handleGrafanaAlert(dto);
        return ResponseEntity.noContent().build();
    }
}
