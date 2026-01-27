package nu.ndw.realtime.monitoring.controller;

import static nu.ndw.realtime.monitoring.util.TestFixtures.getResourceAsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nu.ndw.realtime.monitoring.dto.TeamTokenResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;

class GrafanaAlertIT extends BaseIT {

    private static final String VALID_REQUEST_SCENARIO = "grafana/valid-requests/%s";

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

    @Test
    @Sql({"classpath:sql/clean.sql", "classpath:sql/service.sql"})
    void testCreateIncidentAndAlert() throws Exception {
        var scenario = VALID_REQUEST_SCENARIO.formatted("create-incident-and-alert-with-single-firing");

        assertThat(incidentRepository.findActiveByServiceId(SERVICE_UUID)).isEmpty();
        assertThat(alertRepository.findByFingerprintId(ALERT_FINGERPRINT)).isEmpty();

        mockMvc.perform(post(GRAFANA_ALERT_PATH.formatted(TEAM_ID))
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
    void testResolveOnlyAlertAndCloseIncident() throws Exception {
        var scenario = VALID_REQUEST_SCENARIO.formatted("resolve-only-alert-and-close-incident");

        assertThat(incidentRepository.findActiveByServiceId(SERVICE_UUID)).isPresent();
        assertThat(alertRepository.findByFingerprintId(ALERT_FINGERPRINT)).isPresent();

        mockMvc.perform(post(GRAFANA_ALERT_PATH.formatted(TEAM_ID))
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
}
