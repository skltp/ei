package se.skltp.ei.writelock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ServiceStatus;
import org.apache.camel.spi.RouteController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.lang.reflect.Field;

class WriteLockStatusProcessorTest {

    private WriteLockStatusProcessor processor;
    private WriteLockService writeLockService;
    private RouteController routeController;
    private Message message;
    private Exchange exchange;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        processor = new WriteLockStatusProcessor();
        writeLockService = mock(WriteLockService.class);
        CamelContext camelContext = mock(CamelContext.class);
        routeController = mock(RouteController.class);
        when(camelContext.getRouteController()).thenReturn(routeController);

        Field serviceField = WriteLockStatusProcessor.class.getDeclaredField("writeLockService");
        serviceField.setAccessible(true);
        serviceField.set(processor, writeLockService);

        Field contextField = WriteLockStatusProcessor.class.getDeclaredField("camelContext");
        contextField.setAccessible(true);
        contextField.set(processor, camelContext);

        message = mock(Message.class);
        exchange = mock(Exchange.class);
        when(exchange.getIn()).thenReturn(message);
        when(writeLockService.getProcessRouteId()).thenReturn("backend-process-route");
        when(writeLockService.getCollectRouteId()).thenReturn("backend-collection-route");
    }

    private JsonNode processAndParse() throws Exception {
        processor.process(exchange);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(message).setBody(captor.capture());
        return objectMapper.readTree(captor.getValue());
    }

    @Test
    void statusWhenDisabled() throws Exception {
        when(writeLockService.isEnabled()).thenReturn(false);
        when(routeController.getRouteStatus("backend-process-route")).thenReturn(ServiceStatus.Started);
        when(routeController.getRouteStatus("backend-collection-route")).thenReturn(ServiceStatus.Started);

        JsonNode json = processAndParse();

        assertFalse(json.get("writeLockEnabled").asBoolean());
        assertEquals("Started", json.get("routes").get("backend-process-route").asText());
        assertEquals("Started", json.get("routes").get("backend-collection-route").asText());
    }

    @Test
    void statusWhenEnabled() throws Exception {
        when(writeLockService.isEnabled()).thenReturn(true);
        when(routeController.getRouteStatus("backend-process-route")).thenReturn(ServiceStatus.Suspended);
        when(routeController.getRouteStatus("backend-collection-route")).thenReturn(ServiceStatus.Suspended);

        JsonNode json = processAndParse();

        assertTrue(json.get("writeLockEnabled").asBoolean());
        assertEquals("Suspended", json.get("routes").get("backend-process-route").asText());
        assertEquals("Suspended", json.get("routes").get("backend-collection-route").asText());
    }

    @Test
    void statusWhenRouteStatusIsNull() throws Exception {
        when(writeLockService.isEnabled()).thenReturn(false);
        when(routeController.getRouteStatus("backend-process-route")).thenReturn(null);
        when(routeController.getRouteStatus("backend-collection-route")).thenReturn(null);

        JsonNode json = processAndParse();

        assertEquals("Unknown", json.get("routes").get("backend-process-route").asText());
        assertEquals("Unknown", json.get("routes").get("backend-collection-route").asText());
    }
}
