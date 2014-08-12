package se.skltp.ei.monitoring;

import java.util.Date;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.ThreadSafeSimpleDateFormat;

import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface;
import se.riv.itintegration.monitoring.v1.ConfigurationType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;
import se.skltp.ei.svc.service.api.FindContentInterface;

@WebService(
		serviceName = "PingForConfigurationResponderService", 
		endpointInterface="se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface", 
		portName = "PingForConfigurationResponderPort", 
		targetNamespace = "urn:riv:itintegration:monitoring:PingForConfiguration:1:rivtabp21",
		wsdlLocation = "ServiceContracts_itintegration_monitoring/interactions/PingForConfigurationInteraction/PingForConfigurationInteraction_1.0_RIVTABP21.wsdl")
public class PingForconfigurationDbCheckWSBean implements PingForConfigurationResponderInterface{
	
	private ThreadSafeSimpleDateFormat dateFormat = new ThreadSafeSimpleDateFormat("yyyyMMddhhmmss");
	
	private static final Logger log = LoggerFactory.getLogger(PingForconfigurationDbCheckWSBean.class);

	private String appName;
	
	/**
	 * Set the application name to be used when responding PingForConfiguration requests
	 * @param appName
	 */
	public void setAppName(String appName){
		this.appName = appName;
	}
	
	private String checkDbRri;
	
	/**
	 * Registered resident identifier to use when checking if database is reachable
	 * @param checkDbRri
	 */
	public void setCheckDbRri(String checkDbRri){
		this.checkDbRri = checkDbRri;
	}
	
	private String checkDbServiceDomain;
	
	/**
	 * Servicedomain to use when checking if database is reachable
	 * @param checkDbServiceDomain
	 */
	public void setCheckDbServiceDomain(String checkDbServiceDomain){
		this.checkDbServiceDomain = checkDbServiceDomain;
	}
	
	private FindContentInterface blBean = null;
	   
	/**
	 * Set FindContentInterface to be able to check if database is reachable
	 * @param blBean
	 */
    public void setBlBean(FindContentInterface blBean) {
    	this.blBean = blBean;
    }
	
	
	@Override
	public PingForConfigurationResponseType pingForConfiguration(String logicalAddress,
			PingForConfigurationType parameters) {
		
		log.info("PingForConfiguration requested for {}", appName);
		
		PingForConfigurationResponseType response = new PingForConfigurationResponseType();
		response.setPingDateTime(dateFormat.format(new Date()));
		response.getConfiguration().add(createConfigurationInfo("Applikation", appName));
		
		log.info("Checking database is reachable for application {}", appName);
		checkDatabaseIsReachable();
		
		log.info("PingForConfiguration response returned for {}", appName);
		
		return response;
	}

	private void checkDatabaseIsReachable() {
		FindContentType request = new FindContentType();
		request.setRegisteredResidentIdentification(checkDbRri);
		request.setServiceDomain(checkDbServiceDomain);
		
		//Make a FindContent request to validate db access
		try {
			blBean.findContent(null, request);
			log.debug("Database is reachable for application {}", appName);
		} catch (Exception e) {
			log.error("Error occured trying to use database for " + appName, e);
			throw new RuntimeException("Error occured trying to use "+ appName + " database, see application logs for details");
		}
	}


	private ConfigurationType createConfigurationInfo(String name, String value) {
		log.debug("PingForConfiguration config added [{}: {}]", name, value);
		
		ConfigurationType configurationInfo = new ConfigurationType();
		configurationInfo.setName(name);
		configurationInfo.setValue(value);
		return configurationInfo;
	}

}
