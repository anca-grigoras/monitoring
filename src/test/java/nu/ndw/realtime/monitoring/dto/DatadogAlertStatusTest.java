package nu.ndw.realtime.monitoring.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DatadogAlertStatusTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @MethodSource("provideValidLabels")
    void fromLabel_withValidLabel_returnsCorrectStatus(String label, DatadogAlertStatus expectedStatus) {
        DatadogAlertStatus result = DatadogAlertStatus.fromLabel(label);
        assertEquals(expectedStatus, result);
    }

    @ParameterizedTest
    @MethodSource("provideCaseInsensitiveLabels")
    void fromLabel_withDifferentCase_returnsCorrectStatus(String label, DatadogAlertStatus expectedStatus) {
        DatadogAlertStatus result = DatadogAlertStatus.fromLabel(label);
        assertEquals(expectedStatus, result);
    }

    @ParameterizedTest
    @NullSource
    void fromLabel_withNull_returnsNull(String label) {
        DatadogAlertStatus result = DatadogAlertStatus.fromLabel(label);
        assertNull(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "invalid", "unknown", "Alert", "ERROR", "   "})
    void fromLabel_withInvalidLabel_returnsNull(String label) {
        DatadogAlertStatus result = DatadogAlertStatus.fromLabel(label);
        assertNull(result);
    }

    @Test
    void getLabel_returnsCorrectLabel() {
        assertEquals("Triggered", DatadogAlertStatus.TRIGGERED.getLabel());
        assertEquals("Warn", DatadogAlertStatus.WARN.getLabel());
        assertEquals("Recovered", DatadogAlertStatus.RECOVERED.getLabel());
        assertEquals("No Data", DatadogAlertStatus.NO_DATA.getLabel());
        assertEquals("Re-Triggered", DatadogAlertStatus.RENOTIFY.getLabel());
    }

    @Test
    void values_containsAllExpectedStatuses() {
        DatadogAlertStatus[] statuses = DatadogAlertStatus.values();
        assertEquals(5, statuses.length);
        assertArrayEquals(
                new DatadogAlertStatus[]{
                        DatadogAlertStatus.TRIGGERED,
                        DatadogAlertStatus.WARN,
                        DatadogAlertStatus.RECOVERED,
                        DatadogAlertStatus.NO_DATA,
                        DatadogAlertStatus.RENOTIFY
                },
                statuses
        );
    }

    @ParameterizedTest
    @MethodSource("provideStatusesForJsonSerialization")
    void jsonValue_serializesCorrectly(DatadogAlertStatus status, String expectedJson) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(status);
        assertEquals(expectedJson, json);
    }

    @ParameterizedTest
    @MethodSource("provideJsonForDeserialization")
    void jsonCreator_deserializesCorrectly(String json, DatadogAlertStatus expectedStatus) throws JsonProcessingException {
        DatadogAlertStatus result = objectMapper.readValue(json, DatadogAlertStatus.class);
        assertEquals(expectedStatus, result);
    }

    @Test
    void jsonCreator_withNull_deserializesToNull() throws JsonProcessingException {
        DatadogAlertStatus result = objectMapper.readValue("null", DatadogAlertStatus.class);
        assertNull(result);
    }

    private static Stream<Arguments> provideValidLabels() {
        return Stream.of(
                Arguments.of("Triggered", DatadogAlertStatus.TRIGGERED),
                Arguments.of("Warn", DatadogAlertStatus.WARN),
                Arguments.of("Recovered", DatadogAlertStatus.RECOVERED),
                Arguments.of("No Data", DatadogAlertStatus.NO_DATA),
                Arguments.of("Re-Triggered", DatadogAlertStatus.RENOTIFY)
        );
    }

    private static Stream<Arguments> provideCaseInsensitiveLabels() {
        return Stream.of(
                Arguments.of("triggered", DatadogAlertStatus.TRIGGERED),
                Arguments.of("TRIGGERED", DatadogAlertStatus.TRIGGERED),
                Arguments.of("TrIgGeReD", DatadogAlertStatus.TRIGGERED),
                Arguments.of("warn", DatadogAlertStatus.WARN),
                Arguments.of("WARN", DatadogAlertStatus.WARN),
                Arguments.of("recovered", DatadogAlertStatus.RECOVERED),
                Arguments.of("RECOVERED", DatadogAlertStatus.RECOVERED),
                Arguments.of("no data", DatadogAlertStatus.NO_DATA),
                Arguments.of("NO DATA", DatadogAlertStatus.NO_DATA),
                Arguments.of("re-triggered", DatadogAlertStatus.RENOTIFY),
                Arguments.of("RE-TRIGGERED", DatadogAlertStatus.RENOTIFY)
        );
    }

    private static Stream<Arguments> provideStatusesForJsonSerialization() {
        return Stream.of(
                Arguments.of(DatadogAlertStatus.TRIGGERED, "\"Triggered\""),
                Arguments.of(DatadogAlertStatus.WARN, "\"Warn\""),
                Arguments.of(DatadogAlertStatus.RECOVERED, "\"Recovered\""),
                Arguments.of(DatadogAlertStatus.NO_DATA, "\"No Data\""),
                Arguments.of(DatadogAlertStatus.RENOTIFY, "\"Re-Triggered\"")
        );
    }

    private static Stream<Arguments> provideJsonForDeserialization() {
        return Stream.of(
                Arguments.of("\"Triggered\"", DatadogAlertStatus.TRIGGERED),
                Arguments.of("\"triggered\"", DatadogAlertStatus.TRIGGERED),
                Arguments.of("\"Warn\"", DatadogAlertStatus.WARN),
                Arguments.of("\"Recovered\"", DatadogAlertStatus.RECOVERED),
                Arguments.of("\"No Data\"", DatadogAlertStatus.NO_DATA),
                Arguments.of("\"Re-Triggered\"", DatadogAlertStatus.RENOTIFY)
        );
    }
}
