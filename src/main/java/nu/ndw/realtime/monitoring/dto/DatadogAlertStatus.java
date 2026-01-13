package nu.ndw.realtime.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DatadogAlertStatus {
    TRIGGERED("Triggered"),
    WARN("Warn"),
    RECOVERED("Recovered"),
    NO_DATA("No Data"),
    RENOTIFY("Re-Triggered");

    private final String label;

    DatadogAlertStatus(String label) {
        this.label = label;
    }

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
