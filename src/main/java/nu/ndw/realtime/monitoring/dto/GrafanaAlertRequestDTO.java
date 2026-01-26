package nu.ndw.realtime.monitoring.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record GrafanaAlertRequestDTO(@Valid @NotEmpty List<Alert> alerts, Map<String, String> commonLabels) {

    public record Alert(
            @NotNull GrafanaAlertStatus status,
            @NotBlank String fingerprint,
            @NotNull Map<String, String> labels,
            Map<String, String> annotations,
            @NotNull Instant startsAt,
            Instant endsAt) {}
}
