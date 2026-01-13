package com.example.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DatadogAlertRequestDTO(
        @NotBlank String id,
        @NotBlank String title,
        @NotBlank String body,
        @NotBlank @JsonProperty("event_type") String eventType,
        @NotBlank String date,
        @JsonProperty("last_updated") String lastUpdated,
        @Valid @NotNull Org org) {

    public record Org(
            @NotBlank String id,
            @NotBlank String name) {}
}
