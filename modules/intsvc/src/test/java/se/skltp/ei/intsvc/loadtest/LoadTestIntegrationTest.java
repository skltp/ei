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
package se.skltp.ei.intsvc.loadtest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.integrationtests.AbstractTestCase;
import se.skltp.ei.intsvc.integrationtests.updateservice.UpdateTestConsumer;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.impl.util.EntityTransformer;

/**
 * Lasttestprogram som simulerar en initialladdning av 4 000 000 index-poster uppdelad på 4000 anrop till update-tjänsten med 1000 transaktioner i varje anrop.
 * 
 * Programmet kan köras i två moder:
 * 
 * 1. Som integrationstest
 * 
 *    När lasttestprogrammet körs som en integrationstest startas mule samt underliggande resurser (databas och meddelandehanterare) som inbäddade resurser i integrationstesten, dvs ingen infrastruktur behöver sättas upp.
 *    Dessa tester lämpar sig bra för att t ex enkelt göra profilering direkt inne i Eclipse av vår kod map utnyttjande av CPU och minne.
 *    NOTE: Då denna test inte skall köras under normala byggen så är den markerad med Ignore, måste mao kommenteras bort tillfälligt för att kunna köras.
 * 
 * 2. Som java applikation
 * 
 *    När lasttestprogrammet körs som en java applikation förväntas såväl mule som underliggande resurser (databas och meddelandehanterare) att vara startade externt från programmet (lokalt på PC eller remote på Test/QA-server).
 *    Dessa tester lämpar sig bra för att göra mer verklighetsliknande prestandatester men med samma lasttestprogram som grund. 
 *    NOTE: URL'er till Update-tjänst måste konfigureras i main-metoden innan programmet körs!
 *    
 * För att inte drukna i loggar från körningen så rekomenderas följande justeringar i fröhållande det som  finns som standard i källkoden på trunk:
 * 
 * 1. composites/svc/src/test/resources/persistence-test.xml
 *    Ändra:
 *      <property name="hibernate.show_sql" value="true"/>
 *    Till:
 *      <property name="hibernate.show_sql" value="false"/>
 *      
 * 2. modules/intsvc/src/test/resources/log4j.xml
 * 
 *    Ändra:
 *      <logger name="org.soitoolkit.commons.mule.messageLogger">
 *		  <level value="DEBUG" />
 *	    </logger>
 *
 *      <logger name="org.hibernate.SQL">
 *        <level value="DEBUG" />
 *      </logger>
 *    
 *    Till:
 *      <logger name="org.soitoolkit.commons.mule.messageLogger">
 *  	  <level value="WARN" />
 *   	</logger>
 *   
 *      <logger name="org.hibernate.SQL">
 *        <level value="INFO" />
 *      </logger>
 *      
 * @author magnus
 */
public class LoadTestIntegrationTest extends AbstractTestCase {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(LoadTestIntegrationTest.class);
	 
	private static final String DEFAULT_UPDATE_SERVICE_ADDRESS = EiMuleServer.getAddress("UPDATE_WEB_SERVICE_URL");

	private static long lastBusinessObjectInstanceId = System.currentTimeMillis();
	
    @PersistenceContext
    private EntityManager entityManager;
    
    private EngagementRepository engagementRepository;

	static {
		System.setProperty("mule.test.timeoutSecs", "3600");
	}
	
	public static void main(String[] args) {
		
		String updateWsUrl = DEFAULT_UPDATE_SERVICE_ADDRESS;
		
		new LoadTestIntegrationTest().performInitialLoad(updateWsUrl);
	}
	
    public LoadTestIntegrationTest() {
        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

    protected String getConfigResources() {
		return 
			"soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
	  		"ei-common.xml," +
	  		"skltp-ei-svc-spring-context.xml," +
	        "get-logical-addressees-service.xml," + 
	        "find-content-service.xml," + 
	        "update-service.xml," + 
	        "notification-service.xml," + 
	        "process-service.xml," + 
//	        "teststub-services/init-dynamic-flows.xml," +
	        "teststub-services/get-logical-addressees-by-service-contract-teststub-service.xml," +
	        "teststub-services/process-notification-teststub-service.xml";
    }

    @Before
    public void setUp() throws Exception {
    	
    	// Lookup the entity repository if not already done
    	if (engagementRepository == null) {
    		engagementRepository = muleContext.getRegistry().lookupObject(EngagementRepository.class);
    	}

    	// Clean the storage
    	engagementRepository.deleteAll();

    	// Clear queues used for the tests
		getJmsUtil().clearQueues(INFO_LOG_QUEUE, ERROR_LOG_QUEUE, PROCESS_QUEUE);
    
    }

	/**
	 * Perform a load test...
	 */
    @Ignore
    @Test
    public void loadTest() {
    			
		System.err.println("### All cnt before: " + engagementRepository.count());

		performInitialLoad(DEFAULT_UPDATE_SERVICE_ADDRESS);

		waitForBackgroundProcessing();
	    
		System.err.println("### All cnt after: " + engagementRepository.count());

    }

	private void performInitialLoad(String defaultUpdateServiceAddress) {
		UpdateTestConsumer consumer = new UpdateTestConsumer(defaultUpdateServiceAddress);
		long totTs = System.currentTimeMillis();
		for (int i=0; i < 4000; i++) {
			long ts = System.currentTimeMillis();
			UpdateResponseType response = consumer.callService("ei-hsa-id", createUdateRequest());
			ts = (System.currentTimeMillis() - ts);
			System.err.println("Elasped #" + i + ": " + ts + " ms. Response: " + response.getResultCode());

			// Assert OK response from the web service
		    if (response.getResultCode() != ResultCodeEnum.OK) {
		    	System.err.println("### UNEXPECTED ERROR CODE: " + response.getResultCode());
		    }
		}
		totTs = (System.currentTimeMillis() - totTs);
		System.err.println("Elapsed Total: " + totTs + " ms.");
	}
    
	private UpdateType createUdateRequest() {

		UpdateType request = new UpdateType();

		for (int i = 0; i < 250; i++) {
			for (int j = 0; j < 4; j++) {
				EngagementTransactionType et = genEngagementTransaction(1000000000+i, "" + (20000000 + ++lastBusinessObjectInstanceId));
				request.getEngagementTransaction().add(et);
			}
		}
		
		return request;
    }

	private EngagementTransactionType genEngagementTransaction(long residentId, String businessObjectInstanceId) {
		
		Engagement entity = genEngagement(residentId, businessObjectInstanceId);
		EngagementType engagement = EntityTransformer.fromEntity(entity);
    	EngagementTransactionType et = new EngagementTransactionType();
    	et.setDeleteFlag(false);
    	et.setEngagement(engagement);
		return et;
	}
	
    private Engagement genEngagement(long residentIdentification, String businessObjectInstanceIdentifier) {
    	final String domain = "urn:riv:dummy:domain";
        final String category = "dummy";
        final String logicalAdress = "SE100200400-600";
        final String sourceSystem = "XXX100200400-600";

        Engagement e = new Engagement();
        e.setBusinessKey("19" + residentIdentification,
                domain,
                category,
                logicalAdress,
                businessObjectInstanceIdentifier,
                sourceSystem,
                "dataController",
                "Inera",
                "NA");

        return e;    	
    }
}