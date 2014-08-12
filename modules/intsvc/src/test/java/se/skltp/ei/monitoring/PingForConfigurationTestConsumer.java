package se.skltp.ei.monitoring;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;

public class PingForConfigurationTestConsumer {

	private static final Logger log = LoggerFactory.getLogger(PingForConfigurationTestConsumer.class);

	PingForConfigurationResponderInterface _service = null;

	public PingForConfigurationTestConsumer(String serviceAddress) {
		JaxWsProxyFactoryBean proxyFactory = new JaxWsProxyFactoryBean();
		proxyFactory.setServiceClass(PingForConfigurationResponderInterface.class);
		proxyFactory.setAddress(serviceAddress);

		_service = (PingForConfigurationResponderInterface) proxyFactory.create();
	}

	public PingForConfigurationResponseType callService(String logicalAddress) throws Exception {
		log.debug("Calling PingForConfiguration soap-service with logicalAddress = {}", logicalAddress);
		PingForConfigurationType request = new PingForConfigurationType();
		request.setLogicalAddress(logicalAddress);
		request.setServiceContractNamespace("urn:riv:itintegration:monitoring:PingForConfigurationResponder:1");
		return _service.pingForConfiguration(logicalAddress, request);
	}

}
