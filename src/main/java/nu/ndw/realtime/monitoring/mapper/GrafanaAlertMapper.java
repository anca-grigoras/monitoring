package nu.ndw.realtime.monitoring.mapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import nu.ndw.realtime.monitoring.dto.GrafanaAlertRequestDTO;
import nu.ndw.realtime.monitoring.dto.GrafanaAlertStatus;
import nu.ndw.realtime.monitoring.model.Alert;
import nu.ndw.realtime.monitoring.model.Metadata;
import nu.ndw.realtime.monitoring.model.Status;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface GrafanaAlertMapper {

    Instant GRAFANA_DEFAULT_ALERT_END_TIME = Instant.parse("0001-01-01T00:00:00Z");

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "incidentId", ignore = true)
    @Mapping(target = "status", source = "alert.status", qualifiedByName = "grafanaStatusMapper")
    @Mapping(target = "fingerprintId", source = "alert.fingerprint")
    @Mapping(target = "environment", source = "alert", qualifiedByName = "extractEnvironment")
    @Mapping(target = "serviceId", source = "alert", qualifiedByName = "extractService")
    @Mapping(target = "alertId", source = "alert", qualifiedByName = "extractAlertId")
    @Mapping(target = "description", source = "alert", qualifiedByName = "extractDescription")
    @Mapping(target = "startTime", source = "alert.startsAt")
    @Mapping(target = "endTime", source = "alert.endsAt", qualifiedByName = "validateGrafanaEndTime")
    Alert map(GrafanaAlertRequestDTO.Alert alert);

    List<Alert> map(List<GrafanaAlertRequestDTO.Alert> alerts);

    @Named("validateGrafanaEndTime")
    default Instant validateGrafanaEndTime(Instant alertEndTime) {
        if (GRAFANA_DEFAULT_ALERT_END_TIME.equals(alertEndTime)) {
            return null;
        }
        return alertEndTime;
    }

    @Named("grafanaStatusMapper")
    default Status grafanaStatusMapper(GrafanaAlertStatus status) {
        return switch (status) {
            case RESOLVED -> Status.NORMAL_OPERATION;
            case FIRING -> Status.ERROR;
        };
    }

    @Named("extractEnvironment")
    default String extractEnvironment(GrafanaAlertRequestDTO.Alert alert) {
        return alert.labels().get("environment");
    }

    @Named("extractService")
    default String extractService(GrafanaAlertRequestDTO.Alert alert) {
        return alert.labels().get("service");
    }

    @Named("extractAlertId")
    default String extractAlertId(GrafanaAlertRequestDTO.Alert alert) {
        return alert.labels().get("alertname");
    }

    @Named("extractDescription")
    default String extractDescription(GrafanaAlertRequestDTO.Alert alert) {
        if (alert.annotations() == null) {
            return null;
        }
        return alert.annotations().get("summary");
    }

    default Metadata mapMetadata(Map<String, String> commonLabels) {
        if (commonLabels == null) {
            return null;
        }
        String alertName = commonLabels.get("alertname");
        return new Metadata(alertName, alertName);
    }
}
