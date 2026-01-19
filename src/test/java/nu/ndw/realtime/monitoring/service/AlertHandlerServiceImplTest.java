package nu.ndw.realtime.monitoring.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import nu.ndw.realtime.monitoring.dto.DatadogAlertRequestDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AlertHandlerServiceImplTest {

    private AlertHandlerServiceImpl alertHandlerService;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        alertHandlerService = new AlertHandlerServiceImpl();

        logger = (Logger) LoggerFactory.getLogger(AlertHandlerServiceImpl.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void handleDatadogAlert_withValidAlert_logsCorrectly() {
        DatadogAlertRequestDTO dto = createDTO(
                "alert-123",
                "High CPU Alert",
                "Triggered",
                "hostname-01",
                "environment:production,service:api",
                "platform"
        );

        alertHandlerService.handleDatadogAlert(dto);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());

        ILoggingEvent logEvent = logsList.get(0);
        assertEquals(Level.INFO, logEvent.getLevel());
        String message = logEvent.getFormattedMessage();
        assertTrue(message.contains("alertId=alert-123"));
        assertTrue(message.contains("status=Triggered"));
        assertTrue(message.contains("title=High CPU Alert"));
        assertTrue(message.contains("environment=production"));
        assertTrue(message.contains("team=platform"));
        assertTrue(message.contains("hostname=hostname-01"));
    }

    @ParameterizedTest
    @MethodSource("provideAlertTransitions")
    void handleDatadogAlert_withDifferentTransitions_logsCorrectStatus(
            String transition, String expectedStatusInLog) {
        DatadogAlertRequestDTO dto = createDTO(
                "alert-123", "Test Alert", transition,
                "host-01", "env:prod", "platform"
        );

        alertHandlerService.handleDatadogAlert(dto);

        String message = listAppender.list.get(0).getFormattedMessage();
        assertTrue(message.contains("status=" + expectedStatusInLog));
    }

    @Test
    void handleDatadogAlert_withUnknownTransition_logsOriginalTransition() {
        DatadogAlertRequestDTO dto = createDTO(
                "alert-123", "Test Alert", "UnknownStatus",
                "host-01", "env:prod", "platform"
        );

        alertHandlerService.handleDatadogAlert(dto);

        String message = listAppender.list.get(0).getFormattedMessage();
        assertTrue(message.contains("status=UnknownStatus"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void handleDatadogAlert_withBlankTags_logsUnknownEnvironment(String tags) {
        DatadogAlertRequestDTO dto = createDTO(
                "alert-123", "Test Alert", "Triggered",
                "host-01", tags, "platform"
        );

        alertHandlerService.handleDatadogAlert(dto);

        String message = listAppender.list.get(0).getFormattedMessage();
        assertTrue(message.contains("environment=unknown"));
    }

    @Test
    void handleDatadogAlert_withTagsWithoutEnvironment_logsUnknownEnvironment() {
        DatadogAlertRequestDTO dto = createDTO(
                "alert-123", "Test Alert", "Triggered",
                "host-01", "service:api,team:platform", "platform"
        );

        alertHandlerService.handleDatadogAlert(dto);

        String message = listAppender.list.get(0).getFormattedMessage();
        assertTrue(message.contains("environment=unknown"));
    }

    @Test
    void handleDatadogAlert_withComplexTags_parsesCorrectly() {
        DatadogAlertRequestDTO dto = createDTO(
                "alert-123", "Test Alert", "Triggered",
                "host-01", "environment:staging,service:api,region:eu-west-1", "platform"
        );

        alertHandlerService.handleDatadogAlert(dto);

        String message = listAppender.list.get(0).getFormattedMessage();
        assertTrue(message.contains("environment=staging"));
    }

    @Test
    void handleDatadogAlert_withDuplicateTagKeys_usesFirstValue() {
        DatadogAlertRequestDTO dto = createDTO(
                "alert-123", "Test Alert", "Triggered",
                "host-01", "environment:production,environment:staging", "platform"
        );

        alertHandlerService.handleDatadogAlert(dto);

        String message = listAppender.list.get(0).getFormattedMessage();
        assertTrue(message.contains("environment=production"));
    }

    @Test
    void handleDatadogAlert_withTagsWithoutColons_ignoresInvalidTags() {
        DatadogAlertRequestDTO dto = createDTO(
                "alert-123", "Test Alert", "Triggered",
                "host-01", "invalidtag,environment:production,anotherbadtag", "platform"
        );

        alertHandlerService.handleDatadogAlert(dto);

        String message = listAppender.list.get(0).getFormattedMessage();
        assertTrue(message.contains("environment=production"));
    }

    @Test
    void handleDatadogAlert_withTagsWithSpaces_trimsCorrectly() {
        DatadogAlertRequestDTO dto = createDTO(
                "alert-123", "Test Alert", "Triggered",
                "host-01", "  environment:production  ,  service:api  ", "platform"
        );

        alertHandlerService.handleDatadogAlert(dto);

        String message = listAppender.list.get(0).getFormattedMessage();
        assertTrue(message.contains("environment=production"));
    }

    @Test
    void handleDatadogAlert_withNullHostname_logsNullHostname() {
        DatadogAlertRequestDTO dto = createDTO(
                "alert-123", "Test Alert", "Triggered",
                null, "environment:production", "platform"
        );

        alertHandlerService.handleDatadogAlert(dto);

        String message = listAppender.list.get(0).getFormattedMessage();
        assertTrue(message.contains("hostname=null"));
    }

    @Test
    void handleDatadogAlert_withColonInTagValue_parsesCorrectly() {
        DatadogAlertRequestDTO dto = createDTO(
                "alert-123", "Test Alert", "Triggered",
                "host-01", "environment:prod:eu:west", "platform"
        );

        alertHandlerService.handleDatadogAlert(dto);

        String message = listAppender.list.get(0).getFormattedMessage();
        assertTrue(message.contains("environment=prod:eu:west"));
    }

    @Test
    void handleDatadogAlert_isImplementingInterface() {
        assertTrue(alertHandlerService instanceof AlertHandlerService);
    }

    @Test
    void alertHandlerServiceImpl_isAnnotatedWithService() {
        assertTrue(AlertHandlerServiceImpl.class.isAnnotationPresent(
                org.springframework.stereotype.Service.class));
    }

    private static Stream<Arguments> provideAlertTransitions() {
        return Stream.of(
                Arguments.of("Triggered", "Triggered"),
                Arguments.of("Warn", "Warn"),
                Arguments.of("Recovered", "Recovered"),
                Arguments.of("No Data", "No Data"),
                Arguments.of("Re-Triggered", "Re-Triggered"),
                Arguments.of("triggered", "Triggered"),
                Arguments.of("RECOVERED", "Recovered")
        );
    }

    private DatadogAlertRequestDTO createDTO(
            String alertId, String alertTitle, String alertTransition,
            String hostname, String tags, String team) {
        return new DatadogAlertRequestDTO(
                alertId,
                alertTitle,
                alertTransition,
                null, // alertQuery
                null, // alertMetric
                null, // alertPriority
                null, // alertScope
                hostname,
                tags,
                null, // link
                "2024-01-15T10:30:00Z",
                null, // orgId
                null, // orgName
                null, // message
                team
        );
    }
}
