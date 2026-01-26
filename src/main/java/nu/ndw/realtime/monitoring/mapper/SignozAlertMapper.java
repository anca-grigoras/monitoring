package nu.ndw.realtime.monitoring.mapper;

import java.time.Instant;
import java.util.List;
import nu.ndw.realtime.monitoring.dto.SignozAlertRequestDTO;
import nu.ndw.realtime.monitoring.dto.SignozAlertStatus;
import nu.ndw.realtime.monitoring.model.Alert;
import nu.ndw.realtime.monitoring.model.Metadata;
import nu.ndw.realtime.monitoring.model.Status;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SignozAlertMapper {

    Instant SIGNOZ_DEFAULT_ALERT_END_TIME = Instant.parse("0001-01-01T00:00:00Z");

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "incidentId", ignore = true)
    @Mapping(target = "status", source = "alert.status", qualifiedByName = "signozStatusMapper")
    @Mapping(target = "fingerprintId", source = "alert.fingerprint")
    @Mapping(target = "environment", source = "alert.labels.environment")
    @Mapping(target = "serviceId", source = "alert.labels.service")
    @Mapping(target = "alertId", source = "alert.labels.ruleId")
    @Mapping(target = "description", source = "alert.annotations.description")
    @Mapping(target = "startTime", source = "alert.startsAt")
    @Mapping(target = "endTime", source = "alert.endsAt", qualifiedByName = "validateSignozEndTime")
    Alert map(SignozAlertRequestDTO.Alert alert);

    List<Alert> map(List<SignozAlertRequestDTO.Alert> alerts);

    @Named("validateSignozEndTime")
    default Instant validateSignozEndTime(Instant alertEndTime) {
        if (SIGNOZ_DEFAULT_ALERT_END_TIME.equals(alertEndTime)) {
            return null;
        }
        return alertEndTime;
    }

    @Named("signozStatusMapper")
    default Status signozStatusMapper(SignozAlertStatus status) {
        return switch (status) {
            case RESOLVED -> Status.NORMAL_OPERATION;
            case FIRING -> Status.ERROR;
        };
    }

    Metadata mapMetadata(SignozAlertRequestDTO.CommonLabels commonLabels);
}
