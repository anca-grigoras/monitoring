package nu.ndw.realtime.monitoring.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Alert(
        UUID id,
        UUID incidentId,
        Status status,
        String fingerprintId,
        String environment,
        String serviceId,
        String alertId,
        String description,
        Instant startTime,
        Instant endTime) {}
