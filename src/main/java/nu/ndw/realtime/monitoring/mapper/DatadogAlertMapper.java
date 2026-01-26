package nu.ndw.realtime.monitoring.mapper;

import java.time.Instant;
import java.util.stream.Stream;
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
    @Mapping(target = "status", source = "alertTransition", qualifiedByName = "datadogStatusMapper")
    @Mapping(target = "fingerprintId", source = "alertId")
    @Mapping(target = "environment", source = "tags", qualifiedByName = "extractEnvironment")
    @Mapping(target = "serviceId", source = "tags", qualifiedByName = "extractService")
    @Mapping(target = "alertId", source = "alertId")
    @Mapping(target = "description", source = "alertTitle")
    @Mapping(target = "startTime", source = "date", qualifiedByName = "parseDatadogDate")
    @Mapping(target = "endTime", expression = "java(resolveDatadogEndTime(dto))")
    Alert map(DatadogAlertRequestDTO dto);

    @Named("datadogStatusMapper")
    default Status datadogStatusMapper(String alertTransition) {
        DatadogAlertStatus status = DatadogAlertStatus.fromLabel(alertTransition);
        if (status == null) {
            return Status.ERROR;
        }
        return switch (status) {
            case RECOVERED -> Status.NORMAL_OPERATION;
            case TRIGGERED, WARN, NO_DATA, RENOTIFY -> Status.ERROR;
        };
    }

    @Named("extractEnvironment")
    default String extractEnvironment(String tags) {
        return extractTagValue(tags, "environment");
    }

    @Named("extractService")
    default String extractService(String tags) {
        return extractTagValue(tags, "service");
    }

    @Named("parseDatadogDate")
    default Instant parseDatadogDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        return Instant.ofEpochSecond(Long.parseLong(date));
    }

    default Instant resolveDatadogEndTime(DatadogAlertRequestDTO dto) {
        DatadogAlertStatus status = DatadogAlertStatus.fromLabel(dto.alertTransition());
        if (status == DatadogAlertStatus.RECOVERED) {
            return parseDatadogDate(dto.date());
        }
        return null;
    }

    default String extractTagValue(String tags, String key) {
        if (tags == null || tags.isBlank()) {
            return null;
        }
        return Stream.of(tags.split(","))
                .map(String::trim)
                .filter(tag -> tag.startsWith(key + ":"))
                .map(tag -> tag.substring(tag.indexOf(':') + 1))
                .findFirst()
                .orElse(null);
    }

    default Metadata mapMetadata(DatadogAlertRequestDTO dto) {
        return new Metadata(dto.alertTitle(), dto.alertId());
    }
}
