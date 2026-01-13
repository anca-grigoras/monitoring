package nu.ndw.realtime.monitoring.service;

import nu.ndw.realtime.monitoring.dto.DatadogAlertRequestDTO;
import nu.ndw.realtime.monitoring.dto.DatadogAlertStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AlertHandlerServiceImpl implements AlertHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(AlertHandlerServiceImpl.class);

    @Override
    public void handleDatadogAlert(DatadogAlertRequestDTO alert) {
        DatadogAlertStatus status = DatadogAlertStatus.fromLabel(alert.alertTransition());
        Map<String, String> tagsMap = parseTags(alert.tags());

        logger.info("Received Datadog alert: alertId={}, status={}, title={}, environment={}, team={}, hostname={}",
                alert.alertId(),
                status != null ? status.getLabel() : alert.alertTransition(),
                alert.alertTitle(),
                tagsMap.getOrDefault("environment", "unknown"),
                alert.team(),
                alert.hostname());
    }

    private Map<String, String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return Collections.emptyMap();
        }
        return Stream.of(tags.split(","))
                .map(String::trim)
                .filter(tag -> tag.contains(":"))
                .collect(Collectors.toMap(
                        tag -> tag.substring(0, tag.indexOf(':')),
                        tag -> tag.substring(tag.indexOf(':') + 1),
                        (existing, replacement) -> existing
                ));
    }
}
