package nu.ndw.realtime.monitoring.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DatadogAlertRequestDTOValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validDTO_noViolations() {
        DatadogAlertRequestDTO dto = createValidDTO();
        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validDTO_withAllOptionalFieldsNull_noViolations() {
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(
                "alert-123",
                "Test Alert",
                "Triggered",
                null, // alertQuery
                null, // alertMetric
                null, // alertPriority
                null, // alertScope
                null, // hostname
                null, // tags
                null, // link
                "2024-01-15T10:30:00Z",
                null, // orgId
                null, // orgName
                null, // message
                "platform"
        );
        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void alertId_whenBlank_hasViolation(String alertId) {
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(
                alertId,
                "Test Alert",
                "Triggered",
                null, null, null, null, null, null, null,
                "2024-01-15T10:30:00Z",
                null, null, null,
                "platform"
        );
        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("alertId")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void alertTitle_whenBlank_hasViolation(String alertTitle) {
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(
                "alert-123",
                alertTitle,
                "Triggered",
                null, null, null, null, null, null, null,
                "2024-01-15T10:30:00Z",
                null, null, null,
                "platform"
        );
        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("alertTitle")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void alertTransition_whenBlank_hasViolation(String alertTransition) {
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(
                "alert-123",
                "Test Alert",
                alertTransition,
                null, null, null, null, null, null, null,
                "2024-01-15T10:30:00Z",
                null, null, null,
                "platform"
        );
        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("alertTransition")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void date_whenBlank_hasViolation(String date) {
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(
                "alert-123",
                "Test Alert",
                "Triggered",
                null, null, null, null, null, null, null,
                date,
                null, null, null,
                "platform"
        );
        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("date")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void team_whenBlank_hasViolation(String team) {
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(
                "alert-123",
                "Test Alert",
                "Triggered",
                null, null, null, null, null, null, null,
                "2024-01-15T10:30:00Z",
                null, null, null,
                team
        );
        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("team")));
    }

    @Test
    void multipleBlankRequiredFields_hasMultipleViolations() {
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(
                null, // alertId
                null, // alertTitle
                null, // alertTransition
                null, null, null, null, null, null, null,
                null, // date
                null, null, null,
                null  // team
        );
        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);
        assertEquals(5, violations.size());
    }

    @Test
    void optionalFields_acceptAnyValue() {
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(
                "alert-123",
                "Test Alert",
                "Triggered",
                "avg(last_5m):avg:system.cpu.user{*} > 80",
                "system.cpu.user",
                "P1",
                "host:web-server-01",
                "web-server-01",
                "env:production,service:api,team:platform",
                "https://app.datadoghq.com/monitors/12345",
                "2024-01-15T10:30:00Z",
                "org-123",
                "MyOrganization",
                "CPU usage is too high on web-server-01",
                "platform"
        );
        Set<ConstraintViolation<DatadogAlertRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void recordAccessors_returnCorrectValues() {
        DatadogAlertRequestDTO dto = createValidDTO();

        assertEquals("alert-123", dto.alertId());
        assertEquals("Test Alert", dto.alertTitle());
        assertEquals("Triggered", dto.alertTransition());
        assertEquals("query", dto.alertQuery());
        assertEquals("metric", dto.alertMetric());
        assertEquals("P1", dto.alertPriority());
        assertEquals("scope", dto.alertScope());
        assertEquals("hostname-01", dto.hostname());
        assertEquals("env:prod,service:api", dto.tags());
        assertEquals("https://example.com", dto.link());
        assertEquals("2024-01-15T10:30:00Z", dto.date());
        assertEquals("org-123", dto.orgId());
        assertEquals("OrgName", dto.orgName());
        assertEquals("Alert message", dto.message());
        assertEquals("platform", dto.team());
    }

    @Test
    void recordEquality_sameValues_areEqual() {
        DatadogAlertRequestDTO dto1 = createValidDTO();
        DatadogAlertRequestDTO dto2 = createValidDTO();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void recordEquality_differentValues_areNotEqual() {
        DatadogAlertRequestDTO dto1 = createValidDTO();
        DatadogAlertRequestDTO dto2 = new DatadogAlertRequestDTO(
                "different-id",
                "Test Alert",
                "Triggered",
                "query", "metric", "P1", "scope", "hostname-01",
                "env:prod,service:api", "https://example.com",
                "2024-01-15T10:30:00Z", "org-123", "OrgName",
                "Alert message", "platform"
        );

        assertNotEquals(dto1, dto2);
    }

    private DatadogAlertRequestDTO createValidDTO() {
        return new DatadogAlertRequestDTO(
                "alert-123",
                "Test Alert",
                "Triggered",
                "query",
                "metric",
                "P1",
                "scope",
                "hostname-01",
                "env:prod,service:api",
                "https://example.com",
                "2024-01-15T10:30:00Z",
                "org-123",
                "OrgName",
                "Alert message",
                "platform"
        );
    }
}
