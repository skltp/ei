package se.skltp.ei.monitoring;

import java.util.Date;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.ThreadSafeSimpleDateFormat;

import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface;
import se.riv.itintegration.monitoring.v1.ConfigurationType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;

@WebService(
		serviceName = "PingForConfigurationResponderService", 
		endpointInterface="se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface", 
		portName = "PingForConfigurationResponderPort", 
		targetNamespace = "urn:riv:itintegration:monitoring:PingForConfiguration:1:rivtabp21",
		wsdlLocation = "ServiceContracts_itintegration_monitoring/interactions/PingForConfigurationInteraction/PingForConfigurationInteraction_1.0_RIVTABP21.wsdl")
public class PingForconfigurationWSBean implements PingForConfigurationResponderInterface{
	
	private ThreadSafeSimpleDateFormat dateFormat = new ThreadSafeSimpleDateFormat("yyyyMMddhhmmss");
	
	private static final Logger log = LoggerFactory.getLogger(PingForconfigurationWSBean.class);

	private String appName;
	
	/**
	 * Set the application name to be used when responding PingForConfiguration requests
	 * @param appName
	 */
	public void setAppName(String appName){
		this.appName = appName;
	}
	
	@Override
	public PingForConfigurationResponseType pingForConfiguration(String logicalAddress,
			PingForConfigurationType parameters) {
		
		log.info("PingForConfiguration requested for {}", appName);
		
		PingForConfigurationResponseType response = new PingForConfigurationResponseType();
		response.setPingDateTime(dateFormat.format(new Date()));
		response.getConfiguration().add(createConfigurationInfo("Applikation", appName));
		
		log.info("PingForConfiguration response returned for {}", appName);
		
		return response;
	}
	
	private ConfigurationType createConfigurationInfo(String name, String value) {
		log.debug("PingForConfiguration config added [{}: {}]", name, value);
		
		ConfigurationType configurationInfo = new ConfigurationType();
		configurationInfo.setName(name);
		configurationInfo.setValue(value);
		return configurationInfo;
	}

}
