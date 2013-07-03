package se.skltp.ei.intsvc.integrationtests.getupdatesservice.registry;

import javax.jws.WebService;

import org.mule.util.StringUtils;

import riv.itintegration.registry.getlogicaladdresseesbyservicecontract._1.rivtabp21.GetLogicalAddresseesByServiceContractResponderInterface;
import riv.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._1.GetLogicalAddresseesByServiceContractResponseType;
import riv.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._1.GetLogicalAddresseesByServiceContractType;

@WebService(serviceName = "GetLogicalAddresseesByServiceContractResponderService", 
			endpointInterface = "riv.itintegration.registry.getlogicaladdresseesbyservicecontract._1.rivtabp21.GetLogicalAddresseesByServiceContractResponderInterface", 
			portName = "GetLogicalAddresseesByServiceContractResponderPort", 
			targetNamespace = "urn:riv:itintegration:registry:GetLogicalAddresseesByServiceContract:1:rivtabp21")
//			wsdlLocation = "schemas/interactions/GetLogicalAddresseesByServiceContractInteraction/GetLogicalAddresseesByServiceContractInteraction_1.0_RIVTABP21.wsdl")
public class GetLogicalAddresseesByServiceContractTestProducer implements
		GetLogicalAddresseesByServiceContractResponderInterface {

	@Override
	public GetLogicalAddresseesByServiceContractResponseType getLogicalAddresseesByServiceContract(
			String logicalAddress, GetLogicalAddresseesByServiceContractType parameters) {

		String serviceConsumerHsaId = parameters.getServiceConsumerHsaId();
		GetLogicalAddresseesByServiceContractResponseType response = new GetLogicalAddresseesByServiceContractResponseType();

		if (StringUtils.equals(serviceConsumerHsaId, "Kalel")) {
			response.getLogicalAddress().add("Lion");
			response.getLogicalAddress().add("Cliff Valley");
			response.getLogicalAddress().add("Aoaoao");
		}

		return response;
	}

}
