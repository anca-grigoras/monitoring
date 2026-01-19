package nu.ndw.realtime.monitoring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nu.ndw.realtime.monitoring.dto.DatadogAlertRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebhookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String WEBHOOK_URL = "/webhooks/alerts/datadog";

    @Test
    void handleDatadogAlert_fullIntegration_returnsNoContent() throws Exception {
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(
                "integration-test-alert-123",
                "Integration Test Alert - High CPU Usage",
                "Triggered",
                "avg(last_5m):avg:system.cpu.user{*} > 80",
                "system.cpu.user",
                "P1",
                "host:web-server-01",
                "web-server-01",
                "environment:production,service:api,team:platform",
                "https://app.datadoghq.com/monitors/12345",
                "2024-01-15T10:30:00Z",
                "org-123",
                "TestOrganization",
                "CPU usage exceeded 80% on web-server-01. Immediate action required.",
                "platform"
        );

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void handleDatadogAlert_triggeredAlert_processesSuccessfully() throws Exception {
        String triggeredAlert = """
                {
                    "alertId": "12345678",
                    "alertTitle": "[Triggered] High CPU Usage on web-server-01",
                    "alertTransition": "Triggered",
                    "alertQuery": "avg(last_5m):avg:system.cpu.user{host:web-server-01} > 80",
                    "alertMetric": "system.cpu.user",
                    "alertPriority": "P1",
                    "alertScope": "host:web-server-01",
                    "hostname": "web-server-01",
                    "tags": "environment:production,service:backend-api,region:eu-west-1",
                    "link": "https://app.datadoghq.com/monitors/12345678",
                    "date": "2024-01-15T10:30:00Z",
                    "orgId": "123456",
                    "orgName": "NDW",
                    "message": "CPU usage has exceeded 80% threshold. Current value: 87.5%",
                    "team": "platform"
                }
                """;

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(triggeredAlert))
                .andExpect(status().isNoContent());
    }

    @Test
    void handleDatadogAlert_recoveredAlert_processesSuccessfully() throws Exception {
        String recoveredAlert = """
                {
                    "alertId": "12345678",
                    "alertTitle": "[Recovered] High CPU Usage on web-server-01",
                    "alertTransition": "Recovered",
                    "alertQuery": "avg(last_5m):avg:system.cpu.user{host:web-server-01} > 80",
                    "alertMetric": "system.cpu.user",
                    "alertPriority": "P1",
                    "alertScope": "host:web-server-01",
                    "hostname": "web-server-01",
                    "tags": "environment:production,service:backend-api,region:eu-west-1",
                    "link": "https://app.datadoghq.com/monitors/12345678",
                    "date": "2024-01-15T11:00:00Z",
                    "orgId": "123456",
                    "orgName": "NDW",
                    "message": "CPU usage has recovered. Current value: 45.2%",
                    "team": "platform"
                }
                """;

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recoveredAlert))
                .andExpect(status().isNoContent());
    }

    @Test
    void handleDatadogAlert_warnAlert_processesSuccessfully() throws Exception {
        String warnAlert = """
                {
                    "alertId": "87654321",
                    "alertTitle": "[Warning] Memory usage approaching limit",
                    "alertTransition": "Warn",
                    "hostname": "app-server-02",
                    "tags": "environment:staging,service:api-gateway",
                    "date": "2024-01-15T14:25:00Z",
                    "team": "infrastructure"
                }
                """;

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(warnAlert))
                .andExpect(status().isNoContent());
    }

    @Test
    void handleDatadogAlert_noDataAlert_processesSuccessfully() throws Exception {
        String noDataAlert = """
                {
                    "alertId": "11223344",
                    "alertTitle": "[No Data] No metrics received from database-01",
                    "alertTransition": "No Data",
                    "hostname": "database-01",
                    "tags": "environment:production,service:postgres,critical:true",
                    "date": "2024-01-15T16:45:00Z",
                    "team": "database"
                }
                """;

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noDataAlert))
                .andExpect(status().isNoContent());
    }

    @Test
    void handleDatadogAlert_renotifyAlert_processesSuccessfully() throws Exception {
        String renotifyAlert = """
                {
                    "alertId": "55667788",
                    "alertTitle": "[Re-Triggered] Disk space critical on storage-01",
                    "alertTransition": "Re-Triggered",
                    "hostname": "storage-01",
                    "tags": "environment:production,service:storage",
                    "date": "2024-01-15T18:00:00Z",
                    "message": "This alert has been re-triggered. Disk usage still at 95%.",
                    "team": "storage"
                }
                """;

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(renotifyAlert))
                .andExpect(status().isNoContent());
    }

    @Test
    void handleDatadogAlert_minimalPayload_processesSuccessfully() throws Exception {
        String minimalAlert = """
                {
                    "alertId": "minimal-123",
                    "alertTitle": "Minimal Alert",
                    "alertTransition": "Triggered",
                    "date": "2024-01-15T10:30:00Z",
                    "team": "test"
                }
                """;

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(minimalAlert))
                .andExpect(status().isNoContent());
    }

    @Test
    void handleDatadogAlert_withSpecialCharactersInMessage_processesSuccessfully() throws Exception {
        String alertWithSpecialChars = """
                {
                    "alertId": "special-chars-123",
                    "alertTitle": "Alert with special <chars> & 'quotes' \\"escaped\\"",
                    "alertTransition": "Triggered",
                    "date": "2024-01-15T10:30:00Z",
                    "message": "Error: Connection failed - timeout after 30s. Retry count: 5/5. Status: CRITICAL!!!",
                    "team": "backend"
                }
                """;

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alertWithSpecialChars))
                .andExpect(status().isNoContent());
    }

    @Test
    void handleDatadogAlert_withUnicodeCharacters_processesSuccessfully() throws Exception {
        String unicodeAlert = """
                {
                    "alertId": "unicode-123",
                    "alertTitle": "Alert: 日本語テスト - Ümlauts and émojis allowed",
                    "alertTransition": "Triggered",
                    "date": "2024-01-15T10:30:00Z",
                    "team": "international"
                }
                """;

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unicodeAlert))
                .andExpect(status().isNoContent());
    }

    @Test
    void handleDatadogAlert_withLongTagString_processesSuccessfully() throws Exception {
        String alertWithManyTags = """
                {
                    "alertId": "many-tags-123",
                    "alertTitle": "Alert with many tags",
                    "alertTransition": "Triggered",
                    "tags": "environment:production,service:api,region:eu-west-1,availability-zone:eu-west-1a,instance-type:c5.xlarge,cluster:main,namespace:default,deployment:v2.3.1,owner:platform-team,cost-center:engineering,project:monitoring,compliance:pci",
                    "date": "2024-01-15T10:30:00Z",
                    "team": "platform"
                }
                """;

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alertWithManyTags))
                .andExpect(status().isNoContent());
    }

    @Test
    void handleDatadogAlert_validationError_returns400() throws Exception {
        String invalidAlert = """
                {
                    "alertId": "",
                    "alertTitle": "Test",
                    "alertTransition": "Triggered",
                    "date": "2024-01-15T10:30:00Z",
                    "team": "platform"
                }
                """;

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidAlert))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleDatadogAlert_missingRequiredField_returns400() throws Exception {
        String missingField = """
                {
                    "alertTitle": "Test",
                    "alertTransition": "Triggered",
                    "date": "2024-01-15T10:30:00Z",
                    "team": "platform"
                }
                """;

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingField))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleDatadogAlert_multipleRequests_allSucceed() throws Exception {
        for (int i = 0; i < 10; i++) {
            String alert = String.format("""
                    {
                        "alertId": "batch-alert-%d",
                        "alertTitle": "Batch Alert #%d",
                        "alertTransition": "Triggered",
                        "date": "2024-01-15T10:%02d:00Z",
                        "team": "batch-team"
                    }
                    """, i, i, i);

            mockMvc.perform(post(WEBHOOK_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(alert))
                    .andExpect(status().isNoContent());
        }
    }
}
