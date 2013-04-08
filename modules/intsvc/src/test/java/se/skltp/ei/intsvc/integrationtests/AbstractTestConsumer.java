package se.skltp.ei.intsvc.integrationtests;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

public abstract class AbstractTestConsumer<ServiceInterface> {

	public static final String SAMPLE_ORIGINAL_CONSUMER_HSAID = "sample-original-consumer-hsaid";
	
	protected ServiceInterface _service = null;	

    private Class<ServiceInterface> _serviceType;

    /**
     * Constructs a test consumer with a web service proxy setup for communication using HTTPS with Mutual Authentication
     * 
     * @param serviceType, required to be able to get the generic class at runtime, see http://stackoverflow.com/questions/3403909/get-generic-type-of-class-at-runtime
     * @param serviceAddress
     */
	public AbstractTestConsumer(Class<ServiceInterface> serviceType, String serviceAddress) {

		_serviceType = serviceType;
		
		JaxWsProxyFactoryBean proxyFactory = new JaxWsProxyFactoryBean();
		proxyFactory.setServiceClass(getServiceType());
		proxyFactory.setAddress(serviceAddress);

		// Used for HTTPS
        /*
		SpringBusFactory bf = new SpringBusFactory();
		URL cxfConfig = this.getClass().getClassLoader().getResource("agp-cxf-test-consumer-config.xml");
		if (cxfConfig != null) {
			proxyFactory.setBus(bf.createBus(cxfConfig));
		}
        */

		_service = proxyFactory.create(getServiceType()); 
	}

    Class<ServiceInterface> getServiceType() {
    	return _serviceType;
    }
}
