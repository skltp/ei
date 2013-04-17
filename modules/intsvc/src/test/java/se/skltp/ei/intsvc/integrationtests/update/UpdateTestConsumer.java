package se.skltp.ei.intsvc.integrationtests.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.integrationtests.AbstractTestConsumer;

public class UpdateTestConsumer extends AbstractTestConsumer<UpdateResponderInterface> {

	private static final Logger log = LoggerFactory.getLogger(UpdateTestConsumer.class);

	public static void main(String[] args) {
		String serviceAddress = EiMuleServer.getAddress("UPDATE_WEB_SERVICE_URL");

		UpdateTestConsumer consumer = new UpdateTestConsumer(serviceAddress);

		UpdateType request = new UpdateType();
		UpdateResponseType response = consumer.callService("logical-adress", request);
        
		log.info("Returned status = " + response.getResultCode());
	}

	public UpdateTestConsumer(String serviceAddress) {
	    
		// Setup a web service proxy for communication using HTTPS with Mutual Authentication
		super(UpdateResponderInterface.class, serviceAddress);
	}

	public UpdateResponseType callService(String logicalAddress, UpdateType request) {

		log.debug("Calling Update-soap-service ");
		

		UpdateResponseType response = _service.update(logicalAddress, request);
        return response;
	}
}
