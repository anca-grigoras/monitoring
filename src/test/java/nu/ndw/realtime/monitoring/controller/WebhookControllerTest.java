package nu.ndw.realtime.monitoring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nu.ndw.realtime.monitoring.dto.DatadogAlertRequestDTO;
import nu.ndw.realtime.monitoring.service.AlertHandlerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlertHandlerService alertHandlerService;

    private static final String WEBHOOK_URL = "/webhooks/alerts/datadog";

    @Test
    void handleDatadogAlert_withValidRequest_returns204NoContent() throws Exception {
        DatadogAlertRequestDTO dto = createValidDTO();

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(alertHandlerService, times(1)).handleDatadogAlert(any(DatadogAlertRequestDTO.class));
    }

    @Test
    void handleDatadogAlert_withMinimalValidRequest_returns204NoContent() throws Exception {
        DatadogAlertRequestDTO dto = new DatadogAlertRequestDTO(
                "alert-123",
                "Test Alert",
                "Triggered",
                null, null, null, null, null, null, null,
                "2024-01-15T10:30:00Z",
                null, null, null,
                "platform"
        );

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());

        verify(alertHandlerService, times(1)).handleDatadogAlert(any(DatadogAlertRequestDTO.class));
    }

    @ParameterizedTest
    @MethodSource("provideMissingRequiredFields")
    void handleDatadogAlert_withMissingRequiredField_returns400BadRequest(
            String requestBody, String expectedFieldInError) throws Exception {

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(alertHandlerService, never()).handleDatadogAlert(any());
    }

    @Test
    void handleDatadogAlert_withEmptyBody_returns400BadRequest() throws Exception {
        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(alertHandlerService, never()).handleDatadogAlert(any());
    }

    @Test
    void handleDatadogAlert_withInvalidJson_returns400BadRequest() throws Exception {
        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(alertHandlerService, never()).handleDatadogAlert(any());
    }

    @Test
    void handleDatadogAlert_withEmptyJsonObject_returns400BadRequest() throws Exception {
        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(alertHandlerService, never()).handleDatadogAlert(any());
    }

    @Test
    void handleDatadogAlert_withBlankAlertId_returns400BadRequest() throws Exception {
        String requestBody = """
                {
                    "alertId": "   ",
                    "alertTitle": "Test Alert",
                    "alertTransition": "Triggered",
                    "date": "2024-01-15T10:30:00Z",
                    "team": "platform"
                }
                """;

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(alertHandlerService, never()).handleDatadogAlert(any());
    }

    @Test
    void handleDatadogAlert_withWrongContentType_returns415UnsupportedMediaType() throws Exception {
        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("some text"))
                .andExpect(status().isUnsupportedMediaType());

        verify(alertHandlerService, never()).handleDatadogAlert(any());
    }

    @Test
    void handleDatadogAlert_withXmlContent_returns415UnsupportedMediaType() throws Exception {
        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_XML)
                        .content("<alert><id>123</id></alert>"))
                .andExpect(status().isUnsupportedMediaType());

        verify(alertHandlerService, never()).handleDatadogAlert(any());
    }

    @Test
    void handleDatadogAlert_getMethod_returns405MethodNotAllowed() throws Exception {
        mockMvc.perform(get(WEBHOOK_URL))
                .andExpect(status().isMethodNotAllowed());

        verify(alertHandlerService, never()).handleDatadogAlert(any());
    }

    @Test
    void handleDatadogAlert_putMethod_returns405MethodNotAllowed() throws Exception {
        mockMvc.perform(put(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());

        verify(alertHandlerService, never()).handleDatadogAlert(any());
    }

    @Test
    void handleDatadogAlert_deleteMethod_returns405MethodNotAllowed() throws Exception {
        mockMvc.perform(delete(WEBHOOK_URL))
                .andExpect(status().isMethodNotAllowed());

        verify(alertHandlerService, never()).handleDatadogAlert(any());
    }

    @Test
    void handleDatadogAlert_whenServiceThrowsException_returns500InternalServerError() throws Exception {
        DatadogAlertRequestDTO dto = createValidDTO();
        doThrow(new RuntimeException("Service error")).when(alertHandlerService)
                .handleDatadogAlert(any(DatadogAlertRequestDTO.class));

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void handleDatadogAlert_withAllFields_passesCorrectDataToService() throws Exception {
        String requestBody = """
                {
                    "alertId": "12345678",
                    "alertTitle": "High CPU Usage Alert",
                    "alertTransition": "Triggered",
                    "alertQuery": "avg(last_5m):avg:system.cpu.user{*} > 80",
                    "alertMetric": "system.cpu.user",
                    "alertPriority": "P1",
                    "alertScope": "host:web-server-01",
                    "hostname": "web-server-01",
                    "tags": "env:production,service:api",
                    "link": "https://app.datadoghq.com/monitors/12345",
                    "date": "2024-01-15T10:30:00Z",
                    "orgId": "org-123",
                    "orgName": "MyOrganization",
                    "message": "CPU usage exceeded 80%",
                    "team": "platform"
                }
                """;

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());

        verify(alertHandlerService).handleDatadogAlert(argThat(dto ->
                dto.alertId().equals("12345678") &&
                dto.alertTitle().equals("High CPU Usage Alert") &&
                dto.alertTransition().equals("Triggered") &&
                dto.hostname().equals("web-server-01") &&
                dto.team().equals("platform")
        ));
    }

    @Test
    void handleDatadogAlert_withDifferentAlertTransitions_acceptsAll() throws Exception {
        String[] transitions = {"Triggered", "Warn", "Recovered", "No Data", "Re-Triggered", "CustomTransition"};

        for (String transition : transitions) {
            String requestBody = String.format("""
                    {
                        "alertId": "alert-123",
                        "alertTitle": "Test Alert",
                        "alertTransition": "%s",
                        "date": "2024-01-15T10:30:00Z",
                        "team": "platform"
                    }
                    """, transition);

            mockMvc.perform(post(WEBHOOK_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNoContent());
        }

        verify(alertHandlerService, times(transitions.length)).handleDatadogAlert(any());
    }

    @Test
    void webhookEndpoint_acceptsApplicationJsonUtf8() throws Exception {
        DatadogAlertRequestDTO dto = createValidDTO();

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType("application/json;charset=UTF-8")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    private static Stream<Arguments> provideMissingRequiredFields() {
        return Stream.of(
                Arguments.of("""
                        {
                            "alertTitle": "Test Alert",
                            "alertTransition": "Triggered",
                            "date": "2024-01-15T10:30:00Z",
                            "team": "platform"
                        }
                        """, "alertId"),
                Arguments.of("""
                        {
                            "alertId": "alert-123",
                            "alertTransition": "Triggered",
                            "date": "2024-01-15T10:30:00Z",
                            "team": "platform"
                        }
                        """, "alertTitle"),
                Arguments.of("""
                        {
                            "alertId": "alert-123",
                            "alertTitle": "Test Alert",
                            "date": "2024-01-15T10:30:00Z",
                            "team": "platform"
                        }
                        """, "alertTransition"),
                Arguments.of("""
                        {
                            "alertId": "alert-123",
                            "alertTitle": "Test Alert",
                            "alertTransition": "Triggered",
                            "team": "platform"
                        }
                        """, "date"),
                Arguments.of("""
                        {
                            "alertId": "alert-123",
                            "alertTitle": "Test Alert",
                            "alertTransition": "Triggered",
                            "date": "2024-01-15T10:30:00Z"
                        }
                        """, "team")
        );
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
