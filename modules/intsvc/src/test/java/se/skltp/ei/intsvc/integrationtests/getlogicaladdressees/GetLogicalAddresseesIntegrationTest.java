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
package se.skltp.ei.intsvc.integrationtests.getlogicaladdressees;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.GetLogicalAddresseesByServiceContractResponseType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.getlogicaladdressees.GetLogicalAddresseesServiceClient;
import se.skltp.ei.intsvc.integrationtests.AbstractTestCase;

public class GetLogicalAddresseesIntegrationTest extends AbstractTestCase {

//	@SuppressWarnings("unused")
//	private static final Logger log = LoggerFactory.getLogger(RequestActivitiesIntegrationTest.class);
	 
    @SuppressWarnings("unused")
	private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("ei-config");

    @SuppressWarnings("unused")
	private static final String LOGICAL_ADDRESS = "logical-address";
	@SuppressWarnings("unused")
	private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
//	private static final String EXPECTED_ERR_INVALID_ID_MSG = "Invalid Id: " + TEST_RR_ID_FAULT_INVALID_ID;
	@SuppressWarnings("unused")
	private static final String SERVICE_ADDRESS = EiMuleServer.getAddress("FIND_CONTENT_WEB_SERVICE_URL");
	
	private GetLogicalAddresseesServiceClient client;
  
    public GetLogicalAddresseesIntegrationTest() {
        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

    protected String getConfigResources() {
		return 
			"soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
	  		"ei-common.xml," +
	        "get-logical-addressees-service.xml," +
			"teststub-services/get-logical-addressees-by-service-contract-teststub-service.xml";
    }

    @Override
    public void doSetUp() throws Exception {

    	// Clear queues used for the tests
		getJmsUtil().clearQueues(INFO_LOG_QUEUE, ERROR_LOG_QUEUE);
		
    	if (client == null) {
    		client = muleContext.getRegistry().lookupObject(GetLogicalAddresseesServiceClient.class);
    	}
		
    }

    /**
	 * Perform a test that is expected to return one hit. 
	 */
    @Test
    public void getLogicalAddresses_Ok() throws Exception {
    	
    	GetLogicalAddresseesByServiceContractResponseType logicalAddresses = client.callService();

    	assertEquals(3, logicalAddresses.getLogicalAddressRecord().size());
    }
}