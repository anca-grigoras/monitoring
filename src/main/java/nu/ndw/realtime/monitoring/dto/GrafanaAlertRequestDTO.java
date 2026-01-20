package nu.ndw.realtime.monitoring.dto;

import java.util.List;
import java.util.Map;

public record GrafanaAlertRequestDTO(
        String receiver,
        String status,
        List<GrafanaAlert> alerts,
        Map<String, String> groupLabels,
        Map<String, String> commonLabels,
        Map<String, String> commonAnnotations,
        String externalURL,
        String version,
        String groupKey,
        Integer truncatedAlerts,
        Long orgId,
        String title,
        String state,
        String message) {
}
