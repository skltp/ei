package se.skltp.ei.intsvc.integrationtests;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import riv.itintegration.registry.getlogicaladdresseesbyservicecontract._1.rivtabp21.GetLogicalAddresseesByServiceContractResponderInterface;
import riv.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._1.GetLogicalAddresseesByServiceContractResponseType;
import riv.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._1.GetLogicalAddresseesByServiceContractType;

@WebService(
        serviceName = "GetLogicalAddresseesByServiceContractResponderService", 
        portName = "GetLogicalAddresseesByServiceContractResponderPort", 
        targetNamespace = "urn:riv:itintegration:registry:GetLogicalAddresseesByServiceContract:1:rivtabp21")
public class GetLogicalAddresseesByServiceContractTestProducer implements GetLogicalAddresseesByServiceContractResponderInterface {

    public static final String TEST_ID_FAULT_TIMEOUT = "0";
    
	private static final Logger log = LoggerFactory.getLogger(GetLogicalAddresseesByServiceContractTestProducer.class);
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("ei-config");
	private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));

	@Override
	public GetLogicalAddresseesByServiceContractResponseType getLogicalAddresseesByServiceContract(
			String logicalAddress,
			GetLogicalAddresseesByServiceContractType request) {

		log.info("GetLogicalAddresseesByServiceContractTestProducer received a GetLogicalAddresseesByServiceContract request for hsa-id {} and service contract {}", request.getServiceConsumerHsaId(), request.getServiceContractNameSpace().getServiceContractNamespace());

        // Force a timeout if timeout Id
        if (TEST_ID_FAULT_TIMEOUT.equals(request.getServiceConsumerHsaId())) forceTimeout();

        GetLogicalAddresseesByServiceContractResponseType response = new GetLogicalAddresseesByServiceContractResponseType();
        response.getLogicalAddress().add("HSA_ID_A");
        response.getLogicalAddress().add("HSA_ID_B");
        response.getLogicalAddress().add("HSA_ID_C");
		return response;
	}

    private void forceTimeout() {
        try {
            log.info("TestProducer force a timeout to happen...");
            Thread.sleep(SERVICE_TIMOUT_MS + 1000);
        } catch (InterruptedException e) {}
    }

}