package nu.ndw.realtime.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SignozAlertStatus {
    @JsonProperty("firing")
    FIRING,
    @JsonProperty("resolved")
    RESOLVED
}
