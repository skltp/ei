package se.skltp.ei.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;

public class PingForconfigurationServiceIntegrationTest extends AbstractTestCase {

	private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("ei-config", "ei-config-override");

	private String urlFrontend = null;
	private String urlBackend = null;

	public PingForconfigurationServiceIntegrationTest() {
		urlFrontend = rb.getString("PINGFORCONFIGURATION_INBOUND_ENDPOINT_FRONTEND");
		urlBackend = rb.getString("PINGFORCONFIGURATION_INBOUND_ENDPOINT_BACKEND");
	}
	
	protected String getConfigResources() {
		return 	"soitoolkit-mule-jms-connector-activemq-embedded.xml," +
				"ei-common.xml," +
				"skltp-ei-svc-spring-context.xml," +
				"PingForConfiguration-rivtabp21-service.xml," +
				"PingForConfiguration-rivtabp21-service-checkdb.xml";
	}

	@Test
	public void pingForConfiguration_frontend_ok() throws Exception {

		PingForConfigurationTestConsumer consumer = new PingForConfigurationTestConsumer(urlFrontend);
		PingForConfigurationResponseType response = consumer.callService("logicalAddress");

		assertNotNull(response.getPingDateTime());
		assertNotNull(response.getConfiguration().get(0).getValue());
		assertEquals("Applikation", response.getConfiguration().get(0).getName());
		assertEquals(rb.getString("APPLICATION_NAME_FRONTEND"), response.getConfiguration().get(0).getValue());
	}
	
	@Test
	public void pingForConfiguration_backend_ok() throws Exception {

		PingForConfigurationTestConsumer consumer = new PingForConfigurationTestConsumer(urlBackend);
		PingForConfigurationResponseType response = consumer.callService("logicalAddress");

		assertNotNull(response.getPingDateTime());
		assertNotNull(response.getConfiguration().get(0).getValue());
		assertEquals("Applikation", response.getConfiguration().get(0).getName());
		assertEquals(rb.getString("APPLICATION_NAME_BACKEND"), response.getConfiguration().get(0).getValue());
	}

}
