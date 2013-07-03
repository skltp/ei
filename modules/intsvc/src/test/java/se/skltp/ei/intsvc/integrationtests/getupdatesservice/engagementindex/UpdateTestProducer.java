package se.skltp.ei.intsvc.integrationtests.getupdatesservice.engagementindex;

import javax.jws.WebService;

import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

@WebService(serviceName = "UpdateResponderService", 
			endpointInterface = "riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface",
			portName = "UpdateResponderPort", 
			targetNamespace = "urn:riv:itintegration:engagementindex:Update:1:rivtabp21")
//			wsdlLocation = "schemas/interactions/UpdateInteraction/UpdateInteraction_1.0_RIVTABP21.wsdl")
public class UpdateTestProducer implements UpdateResponderInterface {

	@Override
	public UpdateResponseType update(String arg0, UpdateType arg1) {
		
		UpdateResponseType response = new UpdateResponseType();
        response.setResultCode(ResultCodeEnum.OK);
		
		return response;
	}

}
