package nu.ndw.realtime.monitoring.dto;

import jakarta.validation.constraints.NotBlank;

public record DatadogAlertRequestDTO(
        @NotBlank String alertId,
        @NotBlank String alertTitle,
        @NotBlank String alertTransition,
        String alertQuery,
        String alertMetric,
        String alertPriority,
        String alertScope,
        String hostname,
        String tags,
        String link,
        @NotBlank String date,
        String orgId,
        String orgName,
        String message) {
}
