package nu.ndw.realtime.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum GrafanaAlertStatus {
    @JsonProperty("firing")
    FIRING,
    @JsonProperty("resolved")
    RESOLVED
}
