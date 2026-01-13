package com.example.monitoring.service;

import com.example.monitoring.dto.DatadogAlertRequestDTO;

public interface AlertHandlerService {

    void handleDatadogAlert(DatadogAlertRequestDTO alert);
}
