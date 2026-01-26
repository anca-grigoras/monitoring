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

class GrafanaAlertRequestDTOTest {

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

    private GrafanaAlertRequestDTO.Labels createValidLabels() {
        return new GrafanaAlertRequestDTO.Labels("production", "rule-1", "service-a");
    }

    private GrafanaAlertRequestDTO.Annotations createValidAnnotations() {
        return new GrafanaAlertRequestDTO.Annotations("CPU usage exceeds threshold");
    }

    private GrafanaAlertRequestDTO.Alert createValidAlert() {
        return new GrafanaAlertRequestDTO.Alert(
                GrafanaAlertStatus.FIRING,
                "fingerprint-123",
                createValidLabels(),
                createValidAnnotations(),
                STARTS_AT,
                ENDS_AT);
    }

    private GrafanaAlertRequestDTO.CommonLabels createValidCommonLabels() {
        return new GrafanaAlertRequestDTO.CommonLabels("HighCpuUsage", "rule-1");
    }

    private GrafanaAlertRequestDTO createValidRequest() {
        return new GrafanaAlertRequestDTO(List.of(createValidAlert()), createValidCommonLabels());
    }

    @Test
    void validate_withValidRequest_returnsNoViolations() {
        Set<ConstraintViolation<GrafanaAlertRequestDTO>> violations = validator.validate(createValidRequest());

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_withNullAlerts_returnsViolation() {
        GrafanaAlertRequestDTO dto = new GrafanaAlertRequestDTO(null, createValidCommonLabels());

        Set<ConstraintViolation<GrafanaAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("alerts");
    }

    @Test
    void validate_withEmptyAlerts_returnsViolation() {
        GrafanaAlertRequestDTO dto = new GrafanaAlertRequestDTO(Collections.emptyList(), createValidCommonLabels());

        Set<ConstraintViolation<GrafanaAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("alerts");
    }

    @Test
    void validate_withNullAlertStatus_returnsViolation() {
        GrafanaAlertRequestDTO.Alert alert = new GrafanaAlertRequestDTO.Alert(
                null, "fingerprint-123", createValidLabels(), createValidAnnotations(), STARTS_AT, ENDS_AT);
        GrafanaAlertRequestDTO dto = new GrafanaAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<GrafanaAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("alerts[0].status");
    }

    @Test
    void validate_withBlankFingerprint_returnsViolation() {
        GrafanaAlertRequestDTO.Alert alert = new GrafanaAlertRequestDTO.Alert(
                GrafanaAlertStatus.FIRING, "", createValidLabels(), createValidAnnotations(), STARTS_AT, ENDS_AT);
        GrafanaAlertRequestDTO dto = new GrafanaAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<GrafanaAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("alerts[0].fingerprint");
    }

    @Test
    void validate_withNullLabels_returnsViolation() {
        GrafanaAlertRequestDTO.Alert alert = new GrafanaAlertRequestDTO.Alert(
                GrafanaAlertStatus.FIRING, "fingerprint-123", null, createValidAnnotations(), STARTS_AT, ENDS_AT);
        GrafanaAlertRequestDTO dto = new GrafanaAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<GrafanaAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("alerts[0].labels");
    }

    @Test
    void validate_withBlankLabelEnvironment_returnsViolation() {
        GrafanaAlertRequestDTO.Labels labels = new GrafanaAlertRequestDTO.Labels("", "rule-1", "service-a");
        GrafanaAlertRequestDTO.Alert alert = new GrafanaAlertRequestDTO.Alert(
                GrafanaAlertStatus.FIRING, "fingerprint-123", labels, createValidAnnotations(), STARTS_AT, ENDS_AT);
        GrafanaAlertRequestDTO dto = new GrafanaAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<GrafanaAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("alerts[0].labels.environment");
    }

    @Test
    void validate_withBlankLabelRuleId_returnsViolation() {
        GrafanaAlertRequestDTO.Labels labels = new GrafanaAlertRequestDTO.Labels("production", "", "service-a");
        GrafanaAlertRequestDTO.Alert alert = new GrafanaAlertRequestDTO.Alert(
                GrafanaAlertStatus.FIRING, "fingerprint-123", labels, createValidAnnotations(), STARTS_AT, ENDS_AT);
        GrafanaAlertRequestDTO dto = new GrafanaAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<GrafanaAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("alerts[0].labels.ruleId");
    }

    @Test
    void validate_withBlankLabelService_returnsViolation() {
        GrafanaAlertRequestDTO.Labels labels = new GrafanaAlertRequestDTO.Labels("production", "rule-1", "");
        GrafanaAlertRequestDTO.Alert alert = new GrafanaAlertRequestDTO.Alert(
                GrafanaAlertStatus.FIRING, "fingerprint-123", labels, createValidAnnotations(), STARTS_AT, ENDS_AT);
        GrafanaAlertRequestDTO dto = new GrafanaAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<GrafanaAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("alerts[0].labels.service");
    }

    @Test
    void validate_withNullAnnotations_returnsViolation() {
        GrafanaAlertRequestDTO.Alert alert = new GrafanaAlertRequestDTO.Alert(
                GrafanaAlertStatus.FIRING, "fingerprint-123", createValidLabels(), null, STARTS_AT, ENDS_AT);
        GrafanaAlertRequestDTO dto = new GrafanaAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<GrafanaAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("alerts[0].annotations");
    }

    @Test
    void validate_withBlankAnnotationDescription_returnsViolation() {
        GrafanaAlertRequestDTO.Annotations annotations = new GrafanaAlertRequestDTO.Annotations("");
        GrafanaAlertRequestDTO.Alert alert = new GrafanaAlertRequestDTO.Alert(
                GrafanaAlertStatus.FIRING, "fingerprint-123", createValidLabels(), annotations, STARTS_AT, ENDS_AT);
        GrafanaAlertRequestDTO dto = new GrafanaAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<GrafanaAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("alerts[0].annotations.description");
    }

    @Test
    void validate_withNullStartsAt_returnsViolation() {
        GrafanaAlertRequestDTO.Alert alert = new GrafanaAlertRequestDTO.Alert(
                GrafanaAlertStatus.FIRING,
                "fingerprint-123",
                createValidLabels(),
                createValidAnnotations(),
                null,
                ENDS_AT);
        GrafanaAlertRequestDTO dto = new GrafanaAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<GrafanaAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("alerts[0].startsAt");
    }

    @Test
    void validate_withNullEndsAt_returnsNoViolations() {
        GrafanaAlertRequestDTO.Alert alert = new GrafanaAlertRequestDTO.Alert(
                GrafanaAlertStatus.FIRING,
                "fingerprint-123",
                createValidLabels(),
                createValidAnnotations(),
                STARTS_AT,
                null);
        GrafanaAlertRequestDTO dto = new GrafanaAlertRequestDTO(List.of(alert), createValidCommonLabels());

        Set<ConstraintViolation<GrafanaAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_withBlankCommonLabelsAlertName_returnsViolation() {
        GrafanaAlertRequestDTO.CommonLabels commonLabels = new GrafanaAlertRequestDTO.CommonLabels("", "rule-1");
        GrafanaAlertRequestDTO dto = new GrafanaAlertRequestDTO(List.of(createValidAlert()), commonLabels);

        Set<ConstraintViolation<GrafanaAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("commonLabels.alertName");
    }

    @Test
    void validate_withBlankCommonLabelsRuleId_returnsViolation() {
        GrafanaAlertRequestDTO.CommonLabels commonLabels =
                new GrafanaAlertRequestDTO.CommonLabels("HighCpuUsage", "");
        GrafanaAlertRequestDTO dto = new GrafanaAlertRequestDTO(List.of(createValidAlert()), commonLabels);

        Set<ConstraintViolation<GrafanaAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("commonLabels.ruleId");
    }

    @Test
    void validate_withNullCommonLabels_returnsNoViolations() {
        GrafanaAlertRequestDTO dto = new GrafanaAlertRequestDTO(List.of(createValidAlert()), null);

        Set<ConstraintViolation<GrafanaAlertRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void jsonDeserialization_withValidJson_returnsCorrectDto() throws Exception {
        String json =
                """
                {
                    "alerts": [{
                        "status": "firing",
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

        GrafanaAlertRequestDTO result = objectMapper.readValue(json, GrafanaAlertRequestDTO.class);

        assertThat(result.alerts()).hasSize(1);
        GrafanaAlertRequestDTO.Alert alert = result.alerts().get(0);
        assertThat(alert.status()).isEqualTo(GrafanaAlertStatus.FIRING);
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
    void jsonDeserialization_withAllGrafanaStatuses_returnsCorrectStatus() throws Exception {
        assertThat(deserializeStatus("firing")).isEqualTo(GrafanaAlertStatus.FIRING);
        assertThat(deserializeStatus("resolved")).isEqualTo(GrafanaAlertStatus.RESOLVED);
    }

    @Test
    void jsonDeserialization_withNullEndsAt_returnsNull() throws Exception {
        String json =
                """
                {
                    "alerts": [{
                        "status": "firing",
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

        GrafanaAlertRequestDTO result = objectMapper.readValue(json, GrafanaAlertRequestDTO.class);

        assertThat(result.alerts().get(0).endsAt()).isNull();
    }

    @Test
    void jsonSerialization_withCommonLabels_usesJsonPropertyAnnotation() throws Exception {
        GrafanaAlertRequestDTO dto = createValidRequest();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"alertname\"");
        assertThat(json).doesNotContain("\"alertName\"");
    }

    @Test
    void jsonDeserialization_withResolvedStatus_returnsCorrectDto() throws Exception {
        String json =
                """
                {
                    "alerts": [{
                        "status": "resolved",
                        "fingerprint": "fp-001",
                        "labels": {
                            "environment": "staging",
                            "ruleId": "rule-2",
                            "service": "service-b"
                        },
                        "annotations": {
                            "description": "Issue resolved"
                        },
                        "startsAt": "2025-01-15T10:00:00Z",
                        "endsAt": "2025-01-15T11:00:00Z"
                    }],
                    "commonLabels": {
                        "alertname": "ServiceDown",
                        "ruleId": "rule-2"
                    }
                }
                """;

        GrafanaAlertRequestDTO result = objectMapper.readValue(json, GrafanaAlertRequestDTO.class);

        assertThat(result.alerts().get(0).status()).isEqualTo(GrafanaAlertStatus.RESOLVED);
    }

    private GrafanaAlertStatus deserializeStatus(String statusValue) throws Exception {
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

        GrafanaAlertRequestDTO result = objectMapper.readValue(json, GrafanaAlertRequestDTO.class);
        return result.alerts().get(0).status();
    }
}
