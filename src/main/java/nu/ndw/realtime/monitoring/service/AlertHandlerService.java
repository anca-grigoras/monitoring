package nu.ndw.realtime.monitoring.service;

import nu.ndw.realtime.monitoring.dto.DatadogAlertRequestDTO;
import nu.ndw.realtime.monitoring.dto.GrafanaAlertRequestDTO;

public interface AlertHandlerService {

    void handleDatadogAlert(DatadogAlertRequestDTO alert);

    void handleGrafanaAlert(GrafanaAlertRequestDTO alert);
}
