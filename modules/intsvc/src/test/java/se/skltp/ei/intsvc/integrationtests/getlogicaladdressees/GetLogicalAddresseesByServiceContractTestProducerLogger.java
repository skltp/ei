package se.skltp.ei.intsvc.integrationtests.getlogicaladdressees;

import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skltp.ei.intsvc.EiConstants;

public class GetLogicalAddresseesByServiceContractTestProducerLogger extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(GetLogicalAddresseesByServiceContractTestProducerLogger.class);

	private static String lastOriginalConsumer = null;
	
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		@SuppressWarnings("unchecked")
		Map<String, Object> httpHeaders = (Map<String, Object>)message.getInboundProperty("http.headers");
		
		String orgConsumer = (String)httpHeaders.get(EiConstants.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
		log.info("Test producer called with {}: {}", EiConstants.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, orgConsumer);
		lastOriginalConsumer = orgConsumer;

		return message;
	}

	public static String getLastOriginalConsumer() {
		return lastOriginalConsumer;
	}
}
