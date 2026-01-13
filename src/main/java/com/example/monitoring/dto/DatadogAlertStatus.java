package com.example.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DatadogAlertStatus {
    TRIGGERED("Triggered"),
    WARN("Warn"),
    RECOVERED("Recovered"),
    NO_DATA("No Data"),
    RENOTIFY("Re-Triggered");

    private final String label;

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static DatadogAlertStatus fromLabel(String label) {
        if (label == null) {
            return null;
        }
        for (DatadogAlertStatus status : values()) {
            if (status.label.equalsIgnoreCase(label)) {
                return status;
            }
        }
        return null;
    }
}
