package nu.ndw.realtime.monitoring.service;

import java.util.List;
import java.util.UUID;
import nu.ndw.realtime.monitoring.model.Alert;
import nu.ndw.realtime.monitoring.model.Metadata;

public interface AlertHandlerService {

    void handleAlerts(UUID teamId, List<Alert> alerts, Metadata metadata);
}
