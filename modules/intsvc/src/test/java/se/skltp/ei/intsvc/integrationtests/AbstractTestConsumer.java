/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.intsvc.integrationtests;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;

import se.skltp.ei.intsvc.EiConstants;

public abstract class AbstractTestConsumer<ServiceInterface> {
	
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

		// add http-headers to request
		Client proxy = ClientProxy.getClient(_service);
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		headers.put(EiConstants.X_RIVTA_ORIGINAL_CONSUMER_ID, Arrays.asList("testOriginalConsumerId-1"));
		headers.put(EiConstants.X_SKLTP_CORRELATION_ID, Arrays.asList("testSkltpCorrelationId-1"));
		proxy.getRequestContext().put(Message.PROTOCOL_HEADERS, headers);
	}

    Class<ServiceInterface> getServiceType() {
    	return _serviceType;
    }
}
