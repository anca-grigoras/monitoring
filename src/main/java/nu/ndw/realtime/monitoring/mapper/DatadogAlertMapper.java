package nu.ndw.realtime.monitoring.mapper;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import nu.ndw.realtime.monitoring.dto.DatadogAlertRequestDTO;
import nu.ndw.realtime.monitoring.dto.DatadogAlertStatus;
import nu.ndw.realtime.monitoring.model.Alert;
import nu.ndw.realtime.monitoring.model.Metadata;
import nu.ndw.realtime.monitoring.model.Status;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DatadogAlertMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "incidentId", ignore = true)
    @Mapping(target = "status", source = "alert.alertTransition", qualifiedByName = "datadogStatusMapper")
    @Mapping(target = "fingerprintId", source = "alert", qualifiedByName = "mapFingerprint")
    @Mapping(target = "environment", source = "alert.environment")
    @Mapping(target = "serviceId", source = "alert.tags", qualifiedByName = "extractServiceFromTags")
    @Mapping(target = "alertId", source = "alert.alertId")
    @Mapping(target = "description", source = "alert.alertTitle")
    @Mapping(target = "startTime", source = "alert.date", qualifiedByName = "epochMillisToInstant")
    @Mapping(target = "endTime", ignore = true)
    Alert map(DatadogAlertRequestDTO.Alert alert);

    List<Alert> map(List<DatadogAlertRequestDTO.Alert> alerts);

    @Named("epochMillisToInstant")
    default Instant epochMillisToInstant(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        return Instant.ofEpochMilli(epochMillis);
    }

    @Named("mapFingerprint")
    default String mapFingerprint(DatadogAlertRequestDTO.Alert alert) {
        if (alert.alertScope() != null && !alert.alertScope().isBlank()) {
            return alert.alertId() + "-" + alert.alertScope();
        }
        return alert.alertId();
    }

    @Named("datadogStatusMapper")
    default Status datadogStatusMapper(DatadogAlertStatus status) {
        return switch (status) {
            case RECOVERED -> Status.NORMAL_OPERATION;
            case TRIGGERED, WARN, NO_DATA, RENOTIFY -> Status.ERROR;
        };
    }

    @Named("extractServiceFromTags")
    default String extractServiceFromTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return null;
        }
        return Arrays.stream(tags.split(","))
                .filter(tag -> tag.startsWith("service:"))
                .map(tag -> tag.substring("service:".length()))
                .findFirst()
                .orElse(null);
    }

    Metadata mapMetadata(DatadogAlertRequestDTO.CommonLabels commonLabels);
}
