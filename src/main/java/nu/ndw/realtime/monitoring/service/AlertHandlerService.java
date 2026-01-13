package nu.ndw.realtime.monitoring.service;

import nu.ndw.realtime.monitoring.dto.DatadogAlertRequestDTO;

public interface AlertHandlerService {

    void handleDatadogAlert(DatadogAlertRequestDTO alert);
}
