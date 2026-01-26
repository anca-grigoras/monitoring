package nu.ndw.realtime.monitoring.controller;

import static nu.ndw.realtime.monitoring.util.TestFixtures.getResourceAsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import nu.ndw.realtime.monitoring.dto.TeamTokenResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;

class DatadogAlertIT extends BaseIT {

    private static final String VALID_REQUEST_SCENARIO = "datadog/valid-requests/%s";
    private static final String INVALID_REQUEST_SCENARIO = "datadog/invalid-requests/%s";

    private String teamToken;

    @BeforeEach
    void initTeamToken() throws Exception {
        renewTeamToken();
    }

    private void renewTeamToken() throws Exception {
        var response = mockMvc.perform(
                        get(TEAMS_PATH + "/%s/token".formatted(TEAM_ID)).with(jwt().jwt(jwt -> jwt())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        teamToken = objectMapper.readValue(response, TeamTokenResponseDTO.class).token();
    }

    static Stream<Arguments> invalidRequests() {
        return Stream.of(
                arguments("AlertDescriptionNull", INVALID_REQUEST_SCENARIO.formatted("alert-description-null")),
                arguments("NoAlerts", INVALID_REQUEST_SCENARIO.formatted("no-alerts")),
                arguments("AlertWithoutLabel", INVALID_REQUEST_SCENARIO.formatted("alert-without-label")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRequests")
    @Sql({"classpath:sql/clean.sql", "classpath:sql/service.sql"})
    void testInvalidRequests(String testName, String scenario) throws Exception {

        var result = mockMvc.perform(post(DATADOG_ALERT_PATH.formatted(TEAM_ID))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + teamToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(getResourceAsString(SCENARIO_REQUEST_JSON.formatted(scenario))))
                .andExpect(status().isBadRequest())
                .andReturn();

        compareJsonMaps(
                stringToJsonMap(result.getResponse().getContentAsString()),
                fileToJsonMap(SCENARIO_RESPONSE.formatted(scenario)));
    }

    @Test
    @Sql({"classpath:sql/clean.sql", "classpath:sql/service.sql"})
    void testUnknownTeamId() throws Exception {
        var scenario = VALID_REQUEST_SCENARIO.formatted("unknown-team-id");

        mockMvc.perform(post(DATADOG_ALERT_PATH.formatted("6160be32-bbf8-46e6-9940-d2caed4de5d0"))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + teamToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(getResourceAsString(SCENARIO_REQUEST_JSON.formatted(scenario))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Sql({"classpath:sql/clean.sql", "classpath:sql/service.sql"})
    void testCreateIncidentAndAlert() throws Exception {
        var scenario = VALID_REQUEST_SCENARIO.formatted("create-incident-and-alert-with-single-firing");

        assertThat(incidentRepository.findActiveByServiceId(SERVICE_UUID)).isEmpty();
        assertThat(alertRepository.findByFingerprintId(ALERT_FINGERPRINT)).isEmpty();

        mockMvc.perform(post(DATADOG_ALERT_PATH.formatted(TEAM_ID))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + teamToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(getResourceAsString(SCENARIO_REQUEST_JSON.formatted(scenario))))
                .andExpect(status().isNoContent());

        compareJsonMaps(
                objectToJsonMap(
                        incidentRepository.findActiveByServiceId(SERVICE_UUID).get()),
                fileToJsonMap(SCENARIO_DATABASE_TABLE.formatted(scenario, "incident")),
                "id",
                "service_id");

        compareJsonMaps(
                objectToJsonMap(
                        alertRepository.findByFingerprintId(ALERT_FINGERPRINT).get()),
                fileToJsonMap(SCENARIO_DATABASE_TABLE.formatted(scenario, "alert")),
                "id",
                "incidentId",
                "serviceId",
                "environment",
                "status");
    }

    @Test
    @Sql({
        "classpath:sql/clean.sql",
        "classpath:sql/service.sql",
        "classpath:sql/incident.sql",
        "classpath:sql/alert.sql"
    })
    void testReceiveAlreadyExistingFiringAlert() throws Exception {
        var scenario = VALID_REQUEST_SCENARIO.formatted("receive-already-existing-firing-alert");

        assertThat(incidentRepository.findActiveByServiceId(SERVICE_UUID)).isPresent();
        assertThat(alertRepository.findByFingerprintId(ALERT_FINGERPRINT)).isPresent();

        mockMvc.perform(post(DATADOG_ALERT_PATH.formatted(TEAM_ID))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + teamToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(getResourceAsString(SCENARIO_REQUEST_JSON.formatted(scenario))))
                .andExpect(status().isNoContent());

        compareJsonMaps(
                objectToJsonMap(
                        incidentRepository.findActiveByServiceId(SERVICE_UUID).get()),
                fileToJsonMap(SCENARIO_DATABASE_TABLE.formatted(scenario, "incident")),
                "id",
                "service_id",
                "environment");

        compareJsonMaps(
                objectToJsonMap(
                        alertRepository.findByFingerprintId(ALERT_FINGERPRINT).get()),
                fileToJsonMap(SCENARIO_DATABASE_TABLE.formatted(scenario, "alert")),
                "id",
                "incidentId",
                "serviceId",
                "environment",
                "status");
    }

    @Test
    @Sql({"classpath:sql/clean.sql", "classpath:sql/service.sql", "classpath:sql/incident.sql"})
    void testAddToExistingIncident() throws Exception {
        var scenario = VALID_REQUEST_SCENARIO.formatted("create-alert-with-single-firing");

        assertThat(incidentRepository.findActiveByServiceId(SERVICE_UUID)).isPresent();
        assertThat(alertRepository.findByFingerprintId(ALERT_FINGERPRINT)).isEmpty();

        mockMvc.perform(post(DATADOG_ALERT_PATH.formatted(TEAM_ID))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + teamToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(getResourceAsString(SCENARIO_REQUEST_JSON.formatted(scenario))))
                .andExpect(status().isNoContent());

        compareJsonMaps(
                objectToJsonMap(
                        alertRepository.findByFingerprintId(ALERT_FINGERPRINT).get()),
                fileToJsonMap(SCENARIO_DATABASE_TABLE.formatted(scenario, "alert")),
                "id",
                "incidentId",
                "serviceId",
                "environment",
                "status");
    }

    @Test
    @Sql({"classpath:sql/clean.sql", "classpath:sql/service.sql"})
    void testAddToNonExistingService() throws Exception {
        var scenario = VALID_REQUEST_SCENARIO.formatted("add-to-non-existing-service");

        mockMvc.perform(post(DATADOG_ALERT_PATH.formatted(TEAM_ID))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + teamToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(getResourceAsString(SCENARIO_REQUEST_JSON.formatted(scenario))))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql({
        "classpath:sql/clean.sql",
        "classpath:sql/service.sql",
        "classpath:sql/incident.sql",
        "classpath:sql/alert.sql"
    })
    void testResolveOnlyAlertAndCloseIncident() throws Exception {
        var scenario = VALID_REQUEST_SCENARIO.formatted("resolve-only-alert-and-close-incident");

        assertThat(incidentRepository.findActiveByServiceId(SERVICE_UUID)).isPresent();
        assertThat(alertRepository.findByFingerprintId(ALERT_FINGERPRINT)).isPresent();

        mockMvc.perform(post(DATADOG_ALERT_PATH.formatted(TEAM_ID))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + teamToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(getResourceAsString(SCENARIO_REQUEST_JSON.formatted(scenario))))
                .andExpect(status().isNoContent());

        compareJsonMaps(
                objectToJsonMap(incidentRepository.findByServiceId(SERVICE_UUID).get()),
                fileToJsonMap(SCENARIO_DATABASE_TABLE.formatted(scenario, "incident")),
                "id",
                "service_id");

        compareJsonMaps(
                objectToJsonMap(
                        alertRepository.findByFingerprintId(ALERT_FINGERPRINT).get()),
                fileToJsonMap(SCENARIO_DATABASE_TABLE.formatted(scenario, "alert")),
                "id",
                "incidentId",
                "serviceId",
                "environment",
                "status");
    }

    @Test
    @Sql({
        "classpath:sql/clean.sql",
        "classpath:sql/service.sql",
        "classpath:sql/incident.sql",
        "classpath:sql/multiple-alerts.sql"
    })
    void testResolveOneOfTheAlertsAndKeepIncident() throws Exception {
        var scenario = VALID_REQUEST_SCENARIO.formatted("resolve-one-of-the-alerts-and-keep-incident");

        assertThat(incidentRepository.findActiveByServiceId(SERVICE_UUID)).isPresent();
        assertThat(alertRepository.findByFingerprintId(ALERT_FINGERPRINT)).isPresent();

        mockMvc.perform(post(DATADOG_ALERT_PATH.formatted(TEAM_ID))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + teamToken)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(getResourceAsString(SCENARIO_REQUEST_JSON.formatted(scenario))))
                .andExpect(status().isNoContent());

        assertThat(incidentRepository.findActiveByServiceId(SERVICE_UUID)).isPresent();
        compareJsonMaps(
                objectToJsonMap(
                        incidentRepository.findActiveByServiceId(SERVICE_UUID).get()),
                fileToJsonMap(SCENARIO_DATABASE_TABLE.formatted(scenario, "incident")),
                "id",
                "service_id");
    }
}
