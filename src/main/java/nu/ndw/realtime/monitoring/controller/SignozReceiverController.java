package nu.ndw.realtime.monitoring.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nu.ndw.realtime.monitoring.dto.SignozAlertRequestDTO;
import nu.ndw.realtime.monitoring.mapper.SignozAlertMapper;
import nu.ndw.realtime.monitoring.service.AlertHandlerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/teams/{teamId}/signoz-receiver")
@RequiredArgsConstructor
public class SignozReceiverController {

    private final AlertHandlerService alertHandlerService;
    private final SignozAlertMapper alertMapper;

    @PostMapping
    public ResponseEntity<Void> postAlert(
            @PathVariable UUID teamId, @Valid @RequestBody SignozAlertRequestDTO dto) {
        alertHandlerService.handleAlerts(
                teamId, alertMapper.map(dto.alerts()), alertMapper.mapMetadata(dto.commonLabels()));
        return ResponseEntity.noContent().build();
    }
}
