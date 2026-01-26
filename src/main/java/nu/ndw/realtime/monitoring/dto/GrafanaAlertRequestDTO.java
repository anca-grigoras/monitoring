package nu.ndw.realtime.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GrafanaAlertRequestDTO(
        @NotEmpty @Valid List<Alert> alerts,
        @Valid CommonLabels commonLabels) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Alert(
            @NotNull GrafanaAlertStatus status,
            @Valid Labels labels,
            @Valid Annotations annotations,
            Instant startsAt,
            Instant endsAt,
            @NotBlank String fingerprint) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Labels(
            String alertname,
            String environment,
            String service,
            String ruleId) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Annotations(
            String description,
            String summary) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CommonLabels(
            String alertname,
            String ruleId) {}
}
