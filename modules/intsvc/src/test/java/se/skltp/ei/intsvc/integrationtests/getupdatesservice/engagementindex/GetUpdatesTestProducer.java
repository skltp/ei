package se.skltp.ei.intsvc.integrationtests.getupdatesservice.engagementindex;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.jws.WebService;

import org.mule.util.StringUtils;

import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex.getupdates._1.rivtabp21.GetUpdatesResponderInterface;
import riv.itintegration.engagementindex.getupdatesresponder._1.GetUpdatesResponseType;
import riv.itintegration.engagementindex.getupdatesresponder._1.GetUpdatesType;
import riv.itintegration.engagementindex.getupdatesresponder._1.RegisteredResidentEngagementType;

@WebService(serviceName = "GetUpdatesResponderService",
        endpointInterface = "riv.itintegration.engagementindex.getupdates._1.rivtabp21.GetUpdatesResponderInterface",
        portName = "GetUpdatesResponderPort",
        targetNamespace = "urn:riv:itintegration:engagementindex:GetUpdates:1:rivtabp21")
//        wsdlLocation = "schemas/interactions/GetUpdatesInteraction/GetUpdatesInteraction_1.0_RIVTABP21.wsdl")
public class GetUpdatesTestProducer implements GetUpdatesResponderInterface {

	@Override
	public GetUpdatesResponseType getUpdates(String arg0, GetUpdatesType request) {
		String serviceDomain = request.getServiceDomain();
		GetUpdatesResponseType response = new GetUpdatesResponseType();
		response.setResponseIsComplete(true);
        if (StringUtils.equals(serviceDomain, "riv:crm:scheduling")) {
            // Simulate a partial request - if the previous result set is never sent from the consumer, then this would probably be an infinite loop
            if (StringUtils.isBlank(request.getRegisteredResidentLastFetched())) {
                response.getRegisteredResidentEngagement().add(createRegisteredResidentEngagementType(serviceDomain, "197303160555"));
                response.setResponseIsComplete(false);
            } else {
                response.getRegisteredResidentEngagement().add(createRegisteredResidentEngagementType(serviceDomain, "197707070707"));
                response.setResponseIsComplete(true);
            }
        } else if (StringUtils.equals(serviceDomain, "riv:itintegration:engagementindex")) {
            response.getRegisteredResidentEngagement().add(createRegisteredResidentEngagementType(serviceDomain, "197303160555"));
        } else if (StringUtils.equals(serviceDomain, "riv:crm:scheduling")) {

        } else {
            return null;
        }
		return response;
	}

	private RegisteredResidentEngagementType createRegisteredResidentEngagementType(String serviceDomain, String registeredResidentIdentification) {
		RegisteredResidentEngagementType response = new RegisteredResidentEngagementType();
		response.setRegisteredResidentIdentification(registeredResidentIdentification);
		response.getEngagement().add(createEngagement(serviceDomain, registeredResidentIdentification));
		return response;
	}

	private EngagementType createEngagement(String serviceDomain, String registeredResidentIdentification) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		EngagementType engagement = new EngagementType();
		engagement.setBusinessObjectInstanceIdentifier("bookingId");
		engagement.setCategorization("Booking");
		engagement.setClinicalProcessInterestId(UUID.randomUUID().toString());
		engagement.setCreationTime(dateFormat.format(new Date()));
		engagement.setLogicalAddress("Landstingets hsaid:Vårdgivarens HSA-id:Enhetens hsaid");
		engagement.setMostRecentContent(dateFormat.format(new Date()));
		engagement.setOwner("HSA-id");
		engagement.setRegisteredResidentIdentification(registeredResidentIdentification);
		engagement.setServiceDomain(serviceDomain);
		engagement.setSourceSystem("Systemets HSA-ID");
		engagement.setUpdateTime(dateFormat.format(new Date()));
		return engagement;
	}

}
