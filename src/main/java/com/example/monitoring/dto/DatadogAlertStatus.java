package com.example.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DatadogAlertStatus {
    TRIGGERED("Triggered"),
    WARN("Warn"),
    RECOVERED("Recovered"),
    NO_DATA("No Data");

    private final String label;

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static DatadogAlertStatus fromTitle(String title) {
        if (title == null) {
            return null;
        }
        for (DatadogAlertStatus status : values()) {
            if (title.contains("[" + status.label)) {
                return status;
            }
        }
        return null;
    }
}
