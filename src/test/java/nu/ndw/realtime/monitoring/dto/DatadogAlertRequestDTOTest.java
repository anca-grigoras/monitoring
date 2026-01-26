package nu.ndw.realtime.monitoring.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DatadogAlertRequestDTOTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    private static final Instant STARTS_AT = Instant.parse("2025-01-15T10:00:00Z");
    private static final Instant ENDS_AT = Instant.parse("2025-01-15T11:00:00Z");

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private DatadogAlertRequestDTO.Labels createValidLabels() {
        return new DatadogAlertRequestDTO.Labels("production", "rule-1", "service-a");
    }

    private DatadogAlertRequestDTO.Annotations createValidAnnotations() {
        return new DatadogAlertRequestDTO.Annotations("CPU usage exceeds threshold");
    }

    private DatadogAlertRequestDTO.Alert createValidAlert() {
        return new DatadogAlertRequestDTO.Alert(
                DatadogAlertStatus.TRIGGERED,
                "fingerprint-123",
                createValidLabels(),
                createValidAnnotations(),
                STARTS_AT,
                ENDS_AT);
    }

    private DatadogAlertRequestDTO.CommonLabels createValidCommonLabels() {
        return new DatadogAlertRequestDTO.CommonLabels("HighCpuUsage", "rule-1");
    }

    private DatadogAlertRequestDTO createValidRequest() {
        return new DatadogAlertRequestDTO(List.of(createValidAlert()), createValidCommonLabels());
    }

    @Test
    void validate_withValidRequest_returnsNoViolations() {
        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(createValidRequest());

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_withNullAlerts_returnsViolation() {
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(null, createValidCommonLabels());

        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("alerts");
    }

    @Test
    void validate_withEmptyAlerts_returnsViolation() {
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(Collections.emptyList(), createValidCommonLabels());

        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("alerts");
    }

    @Test
    void validate_withNullAlertStatus_returnsViolation() {
        DatadogAlertRequestDTO.Alert alert = new DatadogAlertRequestDTO.Alert(
                null, "fingerprint-123", createValidLabels(), createValidAnnotations(), STARTS_AT, ENDS_AT);
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("alerts[0].status");
    }

    @Test
    void validate_withBlankFingerprint_returnsViolation() {
        DatadogAlertRequestDTO.Alert alert = new DatadogAlertRequestDTO.Alert(
                DatadogAlertStatus.TRIGGERED, "", createValidLabels(), createValidAnnotations(), STARTS_AT, ENDS_AT);
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("alerts[0].fingerprint");
    }

    @Test
    void validate_withNullLabels_returnsViolation() {
        DatadogAlertRequestDTO.Alert alert = new DatadogAlertRequestDTO.Alert(
                DatadogAlertStatus.TRIGGERED, "fingerprint-123", null, createValidAnnotations(), STARTS_AT, ENDS_AT);
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("alerts[0].labels");
    }

    @Test
    void validate_withBlankLabelEnvironment_returnsViolation() {
        DatadogAlertRequestDTO.Labels labels = new DatadogAlertRequestDTO.Labels("", "rule-1", "service-a");
        DatadogAlertRequestDTO.Alert alert = new DatadogAlertRequestDTO.Alert(
                DatadogAlertStatus.TRIGGERED, "fingerprint-123", labels, createValidAnnotations(), STARTS_AT, ENDS_AT);
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("alerts[0].labels.environment");
    }

    @Test
    void validate_withBlankLabelRuleId_returnsViolation() {
        DatadogAlertRequestDTO.Labels labels = new DatadogAlertRequestDTO.Labels("production", "", "service-a");
        DatadogAlertRequestDTO.Alert alert = new DatadogAlertRequestDTO.Alert(
                DatadogAlertStatus.TRIGGERED, "fingerprint-123", labels, createValidAnnotations(), STARTS_AT, ENDS_AT);
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("alerts[0].labels.ruleId");
    }

    @Test
    void validate_withBlankLabelService_returnsViolation() {
        DatadogAlertRequestDTO.Labels labels = new DatadogAlertRequestDTO.Labels("production", "rule-1", "");
        DatadogAlertRequestDTO.Alert alert = new DatadogAlertRequestDTO.Alert(
                DatadogAlertStatus.TRIGGERED, "fingerprint-123", labels, createValidAnnotations(), STARTS_AT, ENDS_AT);
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("alerts[0].labels.service");
    }

    @Test
    void validate_withNullAnnotations_returnsViolation() {
        DatadogAlertRequestDTO.Alert alert = new DatadogAlertRequestDTO.Alert(
                DatadogAlertStatus.TRIGGERED, "fingerprint-123", createValidLabels(), null, STARTS_AT, ENDS_AT);
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("alerts[0].annotations");
    }

    @Test
    void validate_withBlankAnnotationDescription_returnsViolation() {
        DatadogAlertRequestDTO.Annotations annotations = new DatadogAlertRequestDTO.Annotations("");
        DatadogAlertRequestDTO.Alert alert = new DatadogAlertRequestDTO.Alert(
                DatadogAlertStatus.TRIGGERED, "fingerprint-123", createValidLabels(), annotations, STARTS_AT, ENDS_AT);
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("alerts[0].annotations.description");
    }

    @Test
    void validate_withNullStartsAt_returnsViolation() {
        DatadogAlertRequestDTO.Alert alert = new DatadogAlertRequestDTO.Alert(
                DatadogAlertStatus.TRIGGERED,
                "fingerprint-123",
                createValidLabels(),
                createValidAnnotations(),
                null,
                ENDS_AT);
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("alerts[0].startsAt");
    }

    @Test
    void validate_withNullEndsAt_returnsNoViolations() {
        DatadogAlertRequestDTO.Alert alert = new DatadogAlertRequestDTO.Alert(
                DatadogAlertStatus.TRIGGERED,
                "fingerprint-123",
                createValidLabels(),
                createValidAnnotations(),
                STARTS_AT,
                null);
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_withBlankCommonLabelsAlertName_returnsViolation() {
        DatadogAlertRequestDTO.CommonLabels commonLabels = new DatadogAlertRequestDTO.CommonLabels("", "rule-1");
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(List.of(createValidAlert()), commonLabels);

        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("commonLabels.alertName");
    }

    @Test
    void validate_withBlankCommonLabelsRuleId_returnsViolation() {
        DatadogAlertRequestDTO.CommonLabels commonLabels =
                new DatadogAlertRequestDTO.CommonLabels("HighCpuUsage", "");
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(List.of(createValidAlert()), commonLabels);

        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("commonLabels.ruleId");
    }

    @Test
    void validate_withNullCommonLabels_returnsNoViolations() {
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(List.of(createValidAlert()), null);

        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void jsonDeserialization_withValidJson_returnsCorrectDto() throws Exception {
        String json =
                """
                {
                    "alerts": [{
                        "status": "Triggered",
                        "fingerprint": "fp-001",
                        "labels": {
                            "environment": "production",
                            "ruleId": "rule-1",
                            "service": "service-a"
                        },
                        "annotations": {
                            "description": "High CPU"
                        },
                        "startsAt": "2025-01-15T10:00:00Z",
                        "endsAt": "2025-01-15T11:00:00Z"
                    }],
                    "commonLabels": {
                        "alertname": "HighCpuUsage",
                        "ruleId": "rule-1"
                    }
                }
                """;

        DatadogAlertRequestDTO result = objectMapper.readValue(json, DatadogAlertRequestDTO.class);

        assertThat(result.alerts()).hasSize(1);
        DatadogAlertRequestDTO.Alert alert = result.alerts().get(0);
        assertThat(alert.status()).isEqualTo(DatadogAlertStatus.TRIGGERED);
        assertThat(alert.fingerprint()).isEqualTo("fp-001");
        assertThat(alert.labels().environment()).isEqualTo("production");
        assertThat(alert.labels().ruleId()).isEqualTo("rule-1");
        assertThat(alert.labels().service()).isEqualTo("service-a");
        assertThat(alert.annotations().description()).isEqualTo("High CPU");
        assertThat(alert.startsAt()).isEqualTo(Instant.parse("2025-01-15T10:00:00Z"));
        assertThat(alert.endsAt()).isEqualTo(Instant.parse("2025-01-15T11:00:00Z"));
        assertThat(result.commonLabels().alertName()).isEqualTo("HighCpuUsage");
        assertThat(result.commonLabels().ruleId()).isEqualTo("rule-1");
    }

    @Test
    void jsonDeserialization_withAllDatadogStatuses_returnsCorrectStatus() throws Exception {
        assertThat(deserializeStatus("Triggered")).isEqualTo(DatadogAlertStatus.TRIGGERED);
        assertThat(deserializeStatus("Warn")).isEqualTo(DatadogAlertStatus.WARN);
        assertThat(deserializeStatus("Recovered")).isEqualTo(DatadogAlertStatus.RECOVERED);
        assertThat(deserializeStatus("No Data")).isEqualTo(DatadogAlertStatus.NO_DATA);
        assertThat(deserializeStatus("Re-Triggered")).isEqualTo(DatadogAlertStatus.RENOTIFY);
    }

    @Test
    void jsonDeserialization_withCaseInsensitiveStatus_returnsCorrectStatus() throws Exception {
        assertThat(deserializeStatus("triggered")).isEqualTo(DatadogAlertStatus.TRIGGERED);
        assertThat(deserializeStatus("WARN")).isEqualTo(DatadogAlertStatus.WARN);
        assertThat(deserializeStatus("recovered")).isEqualTo(DatadogAlertStatus.RECOVERED);
    }

    @Test
    void jsonDeserialization_withNullEndsAt_returnsNull() throws Exception {
        String json =
                """
                {
                    "alerts": [{
                        "status": "Triggered",
                        "fingerprint": "fp-001",
                        "labels": {
                            "environment": "production",
                            "ruleId": "rule-1",
                            "service": "service-a"
                        },
                        "annotations": {
                            "description": "High CPU"
                        },
                        "startsAt": "2025-01-15T10:00:00Z"
                    }],
                    "commonLabels": {
                        "alertname": "HighCpuUsage",
                        "ruleId": "rule-1"
                    }
                }
                """;

        DatadogAlertRequestDTO result = objectMapper.readValue(json, DatadogAlertRequestDTO.class);

        assertThat(result.alerts().get(0).endsAt()).isNull();
    }

    @Test
    void jsonSerialization_withCommonLabels_usesJsonPropertyAnnotation() throws Exception {
        DatadogAlertRequestDTO dto = createValidRequest();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"alertname\"");
        assertThat(json).doesNotContain("\"alertName\"");
    }

    private DatadogAlertStatus deserializeStatus(String statusValue) throws Exception {
        String json =
                """
                {
                    "alerts": [{
                        "status": "%s",
                        "fingerprint": "fp-001",
                        "labels": {
                            "environment": "production",
                            "ruleId": "rule-1",
                            "service": "service-a"
                        },
                        "annotations": {
                            "description": "desc"
                        },
                        "startsAt": "2025-01-15T10:00:00Z"
                    }],
                    "commonLabels": {
                        "alertname": "alert",
                        "ruleId": "rule-1"
                    }
                }
                """
                        .formatted(statusValue);

        DatadogAlertRequestDTO result = objectMapper.readValue(json, DatadogAlertRequestDTO.class);
        return result.alerts().get(0).status();
    }
}
