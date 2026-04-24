package se.skltp.ei.writelock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.RouteController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

class WriteLockServiceTest {

    private WriteLockService writeLockService;
    private CamelContext camelContext;
    private RouteController routeController;

    private static final String PROCESS_ROUTE_ID = "backend-process-route";
    private static final String COLLECT_ROUTE_ID = "backend-collection-route";

    @BeforeEach
    void setUp() throws Exception {
        writeLockService = new WriteLockService();
        camelContext = mock(CamelContext.class);
        routeController = mock(RouteController.class);
        when(camelContext.getRouteController()).thenReturn(routeController);

        // Inject mock CamelContext via reflection (field is @Autowired)
        Field field = WriteLockService.class.getDeclaredField("camelContext");
        field.setAccessible(true);
        field.set(writeLockService, camelContext);

        // Inject route IDs via reflection (fields are @Value)
        Field processField = WriteLockService.class.getDeclaredField("processRouteId");
        processField.setAccessible(true);
        processField.set(writeLockService, PROCESS_ROUTE_ID);

        Field collectField = WriteLockService.class.getDeclaredField("collectRouteId");
        collectField.setAccessible(true);
        collectField.set(writeLockService, COLLECT_ROUTE_ID);
    }

    @Test
    void initialStateIsDisabled() {
        assertFalse(writeLockService.isEnabled());
    }

    @Test
    void enableSuspendsRoutes() throws Exception {
        writeLockService.enable();

        assertTrue(writeLockService.isEnabled());
        verify(routeController).suspendRoute(COLLECT_ROUTE_ID);
        verify(routeController).suspendRoute(PROCESS_ROUTE_ID);
    }

    @Test
    void enableIsIdempotent() throws Exception {
        writeLockService.enable();
        writeLockService.enable();

        assertTrue(writeLockService.isEnabled());
        // Routes should only be suspended once
        verify(routeController, times(1)).suspendRoute(COLLECT_ROUTE_ID);
        verify(routeController, times(1)).suspendRoute(PROCESS_ROUTE_ID);
    }

    @Test
    void disableResumesRoutes() throws Exception {
        writeLockService.enable();
        writeLockService.disable();

        assertFalse(writeLockService.isEnabled());
        verify(routeController).resumeRoute(COLLECT_ROUTE_ID);
        verify(routeController).resumeRoute(PROCESS_ROUTE_ID);
    }

    @Test
    void disableIsIdempotent() throws Exception {
        writeLockService.disable();

        assertFalse(writeLockService.isEnabled());
        verify(routeController, never()).resumeRoute(COLLECT_ROUTE_ID);
        verify(routeController, never()).resumeRoute(PROCESS_ROUTE_ID);
    }

    @Test
    void enableThenDisableThenEnableAgain() throws Exception {
        writeLockService.enable();
        assertTrue(writeLockService.isEnabled());

        writeLockService.disable();
        assertFalse(writeLockService.isEnabled());

        writeLockService.enable();
        assertTrue(writeLockService.isEnabled());

        verify(routeController, times(2)).suspendRoute(COLLECT_ROUTE_ID);
        verify(routeController, times(2)).suspendRoute(PROCESS_ROUTE_ID);
        verify(routeController, times(1)).resumeRoute(COLLECT_ROUTE_ID);
        verify(routeController, times(1)).resumeRoute(PROCESS_ROUTE_ID);
    }

    @Test
    void suspendOrderIsCollectFirst() throws Exception {
        var inOrder = org.mockito.Mockito.inOrder(routeController);

        writeLockService.enable();

        inOrder.verify(routeController).suspendRoute(COLLECT_ROUTE_ID);
        inOrder.verify(routeController).suspendRoute(PROCESS_ROUTE_ID);
    }

    @Test
    void resumeOrderIsCollectFirst() throws Exception {
        writeLockService.enable();
        var inOrder = org.mockito.Mockito.inOrder(routeController);

        writeLockService.disable();

        inOrder.verify(routeController).resumeRoute(COLLECT_ROUTE_ID);
        inOrder.verify(routeController).resumeRoute(PROCESS_ROUTE_ID);
    }
}
