package nu.ndw.realtime.monitoring.dto;

import java.util.Map;

public record GrafanaAlert(
        String status,
        Map<String, String> labels,
        Map<String, String> annotations,
        String startsAt,
        String endsAt,
        String generatorURL,
        String fingerprint,
        String silenceURL,
        String dashboardURL,
        String panelURL,
        Map<String, Object> values,
        String valueString) {
}
