package nu.ndw.realtime.monitoring.service;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.realtime.monitoring.model.Alert;
import nu.ndw.realtime.monitoring.model.Metadata;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AlertHandlerServiceImpl implements AlertHandlerService {

    @Override
    public void handleAlerts(UUID teamId, List<Alert> alerts, Metadata metadata) {
        log.info(
                "Received alert '{}' with id '{}' and '{}' number of alerts for team '{}'",
                metadata.alertName(),
                metadata.ruleId(),
                alerts.size(),
                teamId);

        for (var alert : alerts) {
            log.info(
                    "Handling alert for service '{}' on environment '{}' with status '{}'",
                    alert.serviceId(),
                    alert.environment(),
                    alert.status());
        }
        log.info("Finished handling alerts");
    }
}
