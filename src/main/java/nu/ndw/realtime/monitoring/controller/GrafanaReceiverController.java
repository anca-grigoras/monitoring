package nu.ndw.realtime.monitoring.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nu.ndw.realtime.monitoring.dto.GrafanaAlertRequestDTO;
import nu.ndw.realtime.monitoring.mapper.GrafanaAlertMapper;
import nu.ndw.realtime.monitoring.service.AlertHandlerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/teams/{teamId}/grafana-receiver")
@RequiredArgsConstructor
public class GrafanaReceiverController {

    private final AlertHandlerService alertHandlerService;
    private final GrafanaAlertMapper alertMapper;

    @PostMapping
    public ResponseEntity<Void> postAlert(
            @PathVariable UUID teamId, @Valid @RequestBody GrafanaAlertRequestDTO dto) {
        alertHandlerService.handleAlerts(
                teamId, alertMapper.map(dto.alerts()), alertMapper.mapMetadata(dto.commonLabels()));
        return ResponseEntity.noContent().build();
    }
}
