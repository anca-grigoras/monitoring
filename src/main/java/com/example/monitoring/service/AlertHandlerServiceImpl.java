package com.example.monitoring.service;

import com.example.monitoring.dto.DatadogAlertRequestDTO;
import com.example.monitoring.dto.DatadogAlertStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AlertHandlerServiceImpl implements AlertHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(AlertHandlerServiceImpl.class);

    @Override
    public void handleDatadogAlert(DatadogAlertRequestDTO alert) {
        DatadogAlertStatus status = DatadogAlertStatus.fromTitle(alert.title());

        logger.info("Received Datadog alert: id={}, status={}, title={}, org={}",
                alert.id(),
                status != null ? status.getLabel() : "unknown",
                alert.title(),
                alert.org().name());
    }
}
