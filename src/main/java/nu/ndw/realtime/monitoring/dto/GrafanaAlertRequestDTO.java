package nu.ndw.realtime.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public record GrafanaAlertRequestDTO(@Valid @NotEmpty List<Alert> alerts, @Valid CommonLabels commonLabels) {

    public record Alert(
            @NotNull GrafanaAlertStatus status,
            @NotBlank String fingerprint,
            @Valid @NotNull Labels labels,
            @Valid @NotNull Annotations annotations,
            @NotNull Instant startsAt,
            Instant endsAt) {}

    public record CommonLabels(@NotBlank @JsonProperty("alertname") String alertName, @NotBlank String ruleId) {}

    public record Labels(@NotBlank String environment, @NotBlank String ruleId, @NotBlank String service) {}

    public record Annotations(@NotBlank String description) {}
}
