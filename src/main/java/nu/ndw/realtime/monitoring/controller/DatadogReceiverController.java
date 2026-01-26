package nu.ndw.realtime.monitoring.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nu.ndw.realtime.monitoring.dto.DatadogAlertRequestDTO;
import nu.ndw.realtime.monitoring.mapper.DatadogAlertMapper;
import nu.ndw.realtime.monitoring.service.AlertHandlerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/teams/{teamId}/datadog-receiver")
@RequiredArgsConstructor
public class DatadogReceiverController {

    private final AlertHandlerService alertHandlerService;
    private final DatadogAlertMapper alertMapper;

    @PostMapping
    public ResponseEntity<Void> postAlert(
            @PathVariable UUID teamId, @Valid @RequestBody DatadogAlertRequestDTO dto) {
        alertHandlerService.handleAlerts(
                teamId, List.of(alertMapper.map(dto)), alertMapper.mapMetadata(dto));
        return ResponseEntity.noContent().build();
    }
}
