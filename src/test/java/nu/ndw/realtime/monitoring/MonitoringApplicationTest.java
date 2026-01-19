package nu.ndw.realtime.monitoring;

import nu.ndw.realtime.monitoring.controller.WebhookController;
import nu.ndw.realtime.monitoring.service.AlertHandlerService;
import nu.ndw.realtime.monitoring.service.AlertHandlerServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MonitoringApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private WebhookController webhookController;

    @Autowired
    private AlertHandlerService alertHandlerService;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    void webhookControllerBeanExists() {
        assertNotNull(webhookController);
        assertTrue(applicationContext.containsBean("webhookController"));
    }

    @Test
    void alertHandlerServiceBeanExists() {
        assertNotNull(alertHandlerService);
        assertTrue(alertHandlerService instanceof AlertHandlerServiceImpl);
    }

    @Test
    void applicationContextContainsExpectedBeans() {
        assertTrue(applicationContext.containsBean("webhookController"));
        assertTrue(applicationContext.containsBean("alertHandlerServiceImpl"));
    }

    @Test
    void mainMethodDoesNotThrowException() {
        assertDoesNotThrow(() -> MonitoringApplication.main(new String[]{"--server.port=0"}));
    }
}
