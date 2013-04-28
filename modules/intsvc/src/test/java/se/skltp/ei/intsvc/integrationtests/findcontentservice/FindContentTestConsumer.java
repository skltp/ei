package se.skltp.ei.intsvc.integrationtests.findcontentservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex.findcontent._1.rivtabp21.FindContentResponderInterface;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.integrationtests.AbstractTestConsumer;

public class FindContentTestConsumer extends AbstractTestConsumer<FindContentResponderInterface> {

	private static final Logger log = LoggerFactory.getLogger(FindContentTestConsumer.class);

	public static void main(String[] args) {
		String serviceAddress = EiMuleServer.getAddress("FIND_CONTENT_WEB_SERVICE_URL");

		FindContentTestConsumer consumer = new FindContentTestConsumer(serviceAddress);

		FindContentType request = new FindContentType();
		FindContentResponseType response = consumer.callService("logical-adress", request);
        
		log.info("Returned result size = " + response.getEngagement().size());
	}

	public FindContentTestConsumer(String serviceAddress) {
	    
		// Setup a web service proxy for communication using HTTPS with Mutual Authentication
		super(FindContentResponderInterface.class, serviceAddress);
	}

	public FindContentResponseType callService(String logicalAddress, FindContentType request) {

		log.debug("Calling FindContent-soap-service ");
		

		FindContentResponseType response = _service.findContent(logicalAddress, request);
        return response;
	}
}
