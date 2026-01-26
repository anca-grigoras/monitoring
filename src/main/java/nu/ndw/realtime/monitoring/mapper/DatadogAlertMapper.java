package nu.ndw.realtime.monitoring.mapper;

import java.time.Instant;
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

    Instant DATADOG_DEFAULT_ALERT_END_TIME = Instant.parse("0001-01-01T00:00:00Z");

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "incidentId", ignore = true)
    @Mapping(target = "status", source = "alert.status", qualifiedByName = "datadogStatusMapper")
    @Mapping(target = "fingerprintId", source = "alert.fingerprint")
    @Mapping(target = "environment", source = "alert.labels.environment")
    @Mapping(target = "serviceId", source = "alert.labels.service")
    @Mapping(target = "alertId", source = "alert.labels.ruleId")
    @Mapping(target = "description", source = "alert.annotations.description")
    @Mapping(target = "startTime", source = "alert.startsAt")
    @Mapping(target = "endTime", source = "alert.endsAt", qualifiedByName = "validateDatadogEndTime")
    Alert map(DatadogAlertRequestDTO.Alert alert);

    List<Alert> map(List<DatadogAlertRequestDTO.Alert> alerts);

    @Named("validateDatadogEndTime")
    default Instant validateDatadogEndTime(Instant alertEndTime) {
        if (DATADOG_DEFAULT_ALERT_END_TIME.equals(alertEndTime)) {
            return null;
        }
        return alertEndTime;
    }

    @Named("datadogStatusMapper")
    default Status datadogStatusMapper(DatadogAlertStatus status) {
        return switch (status) {
            case RECOVERED -> Status.NORMAL_OPERATION;
            case TRIGGERED, WARN, NO_DATA, RENOTIFY -> Status.ERROR;
        };
    }

    Metadata mapMetadata(DatadogAlertRequestDTO.CommonLabels commonLabels);
}
