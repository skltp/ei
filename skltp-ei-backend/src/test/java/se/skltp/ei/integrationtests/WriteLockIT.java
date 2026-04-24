package se.skltp.ei.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.skltp.ei.EiTeststubRoute.NOTIFICATION_MOCK;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.ServiceStatus;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.skltp.ei.EiBackendApplication;
import se.skltp.ei.EiTeststubRoute;
import se.skltp.ei.entity.repository.EngagementRepository;
import se.skltp.ei.util.EngagementTestUtil.DomainType;
import se.skltp.ei.util.UpdateRequestUtil;
import se.skltp.ei.writelock.WriteLockService;

@CamelSpringBootTest
@SpringBootTest(classes = {EiBackendApplication.class, EiTeststubRoute.class})
@ActiveProfiles("teststub")
class WriteLockIT {

    private static final int RESULT_WAIT_TIMEOUT = 4000;

    @Produce
    protected ProducerTemplate producerTemplate;

    @Autowired
    private WriteLockService writeLockService;

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private EngagementRepository engagementRepository;

    @Value("${ei.hsa.id}")
    String owner;

    @EndpointInject(NOTIFICATION_MOCK)
    private MockEndpoint notificationMock;

    @BeforeEach
    void beforeEach() {
        engagementRepository.deleteAll();
        notificationMock.reset();
    }

    @AfterEach
    void afterEach() throws Exception {
        // Ensure write lock is disabled after each test to not affect other tests
        if (writeLockService.isEnabled()) {
            writeLockService.disable();
        }
    }

    @Test
    void enableWriteLockSuspendsRoutes() throws Exception {
        // Verify routes are started initially
        assertEquals(ServiceStatus.Started, camelContext.getRouteController().getRouteStatus(WriteLockService.PROCESS_ROUTE_ID));
        assertEquals(ServiceStatus.Started, camelContext.getRouteController().getRouteStatus(WriteLockService.COLLECT_ROUTE_ID));
        assertFalse(writeLockService.isEnabled());

        // Enable write lock
        writeLockService.enable();

        // Verify routes are suspended
        assertTrue(writeLockService.isEnabled());
        assertEquals(ServiceStatus.Suspended, camelContext.getRouteController().getRouteStatus(WriteLockService.PROCESS_ROUTE_ID));
        assertEquals(ServiceStatus.Suspended, camelContext.getRouteController().getRouteStatus(WriteLockService.COLLECT_ROUTE_ID));
    }

    @Test
    void disableWriteLockResumesRoutes() throws Exception {
        // Enable then disable
        writeLockService.enable();
        writeLockService.disable();

        // Verify routes are started again
        assertFalse(writeLockService.isEnabled());
        assertEquals(ServiceStatus.Started, camelContext.getRouteController().getRouteStatus(WriteLockService.PROCESS_ROUTE_ID));
        assertEquals(ServiceStatus.Started, camelContext.getRouteController().getRouteStatus(WriteLockService.COLLECT_ROUTE_ID));
    }

    @Test
    void enableIsIdempotent() throws Exception {
        writeLockService.enable();
        writeLockService.enable(); // should be a no-op

        assertTrue(writeLockService.isEnabled());
        assertEquals(ServiceStatus.Suspended, camelContext.getRouteController().getRouteStatus(WriteLockService.PROCESS_ROUTE_ID));
    }

    @Test
    void disableIsIdempotent() throws Exception {
        writeLockService.disable(); // already disabled, should be a no-op

        assertFalse(writeLockService.isEnabled());
        assertEquals(ServiceStatus.Started, camelContext.getRouteController().getRouteStatus(WriteLockService.PROCESS_ROUTE_ID));
    }

    @Test
    void writeLockBlocksMessageProcessing() throws Exception {
        // Enable write lock before sending a message
        writeLockService.enable();

        notificationMock.expectedMessageCount(0);
        notificationMock.setResultWaitTime(RESULT_WAIT_TIMEOUT);

        // Send a message to the process queue — it should NOT be consumed while locked
        final String request = UpdateRequestUtil.createUpdateTxtMsg(owner, DomainType.TWO_SUBSCRIBERS, 1111111111L);
        producerTemplate.sendBody("activemq:queue:{{process.queue.name}}", request);

        // Wait and verify nothing was processed
        notificationMock.assertIsSatisfied();
        assertTrue(engagementRepository.findAll().isEmpty(), "No engagements should be persisted while write lock is enabled");

        // Disable write lock — the buffered message should now be consumed
        notificationMock.reset();
        notificationMock.expectedMessageCount(2);
        notificationMock.setResultWaitTime(10000); // Longer timeout: route resume + consumer reconnect takes time

        writeLockService.disable();

        notificationMock.assertIsSatisfied();
        assertFalse(engagementRepository.findAll().isEmpty(), "Engagements should be persisted after write lock is disabled");
    }

    @Test
    void writeLockHttpEndpointsWork() {
        // Test enable endpoint
        String enableResponse = producerTemplate.requestBody("{{writelock.enable.url}}", "", String.class);
        assertTrue(enableResponse.contains("Write lock enabled"));
        assertTrue(writeLockService.isEnabled());

        // Test status endpoint
        String statusResponse = producerTemplate.requestBody("{{writelock.status.url}}", "", String.class);
        assertTrue(statusResponse.contains("\"writeLockEnabled\" : true"));
        assertTrue(statusResponse.contains("Suspended"));

        // Test disable endpoint
        String disableResponse = producerTemplate.requestBody("{{writelock.disable.url}}", "", String.class);
        assertTrue(disableResponse.contains("Write lock disabled"));
        assertFalse(writeLockService.isEnabled());

        // Verify status shows started again
        statusResponse = producerTemplate.requestBody("{{writelock.status.url}}", "", String.class);
        assertTrue(statusResponse.contains("\"writeLockEnabled\" : false"));
        assertTrue(statusResponse.contains("Started"));
    }
}
