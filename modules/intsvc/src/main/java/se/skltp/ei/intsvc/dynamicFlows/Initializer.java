package se.skltp.ei.intsvc.dynamicFlows;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.config.ConfigurationBuilder;
import org.mule.api.config.MuleProperties;
import org.mule.api.context.MuleContextBuilder;
import org.mule.api.context.MuleContextFactory;
import org.mule.api.context.notification.MuleContextNotificationListener;
import org.mule.config.ConfigResource;
import org.mule.config.spring.SpringXmlConfigurationBuilder;
import org.mule.context.DefaultMuleContextBuilder;
import org.mule.context.DefaultMuleContextFactory;
import org.mule.context.notification.MuleContextNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Sample config:
 * 		<spring:bean id="initializer" class="se.skltp.ei.intsvc.dynamicFlows.Initializer"/>
 *
 * @author magnuslarsson
 *
 */
public class Initializer implements ApplicationContextAware, MuleContextNotificationListener<MuleContextNotification> { // , MuleContextAware {

	private static final Logger log = LoggerFactory.getLogger(Initializer.class);

    private ApplicationContext applicationContext;
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		log.debug("Initializer.setApplicationContext()");
		this.applicationContext = applicationContext;
	}

	static boolean starting = false;
	static boolean stopping = false;
	
	private MuleContext dynamicContext;
	
	public Initializer() {
		log.debug("Initializer object constructed");
	}
	
	public void onNotification(MuleContextNotification notification) {	
		log.debug("Action: {} ({})", notification.getActionName(), notification.getAction());
		if (notification.getAction() == MuleContextNotification.CONTEXT_STARTED) {
			
			if (starting) {
				log.debug("Already starting, no action required!");
				return;
			}

			try {
				
				log.info("Start loading flows...");

				starting = true;

				List<String> flowConfigs = new CreateDynamicFlows().getContextConfiguration();

				log.info("Starting {} flows...", flowConfigs.size());
				
				add(flowConfigs);

				log.info("{} flows started", flowConfigs.size());

			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				starting = false;
			}

		} else if (notification.getAction() == MuleContextNotification.CONTEXT_STOPPING) {
			
			if (stopping) {
				
				log.debug("Already stopping, no further action required!");

				return;
			}
			
			try {
				log.info("Stopping flows...");

				stopping = true;
				
				remove();
				
				log.info("Flows stopped");

			} finally {
				stopping = false;
			}
		}
	}

	private void add(List<String> configs) {
        try {
        	
            String muleId = System.getProperty(MuleProperties.SYSTEM_PROPERTY_PREFIX + "serverId");
            if (muleId != null) System.setProperty(MuleProperties.SYSTEM_PROPERTY_PREFIX + "serverId", muleId + "-dynamic");

            MuleContextFactory muleContextFactory = new DefaultMuleContextFactory();
            List<ConfigurationBuilder> builders = new ArrayList<ConfigurationBuilder>();
            builders.add( springApplicationBuilderUsing(configs));
            MuleContextBuilder contextBuilder = new DefaultMuleContextBuilder();
            MuleContext context = muleContextFactory.createMuleContext(builders, contextBuilder);

            if (muleId != null) System.setProperty(MuleProperties.SYSTEM_PROPERTY_PREFIX + "serverId", muleId);

//            log.info("CONF-CLS: " + context.getConfiguration().getClass());
//            DefaultMuleConfiguration conf = (DefaultMuleConfiguration)context.getConfiguration();
//            conf.setId(conf.getId() + "-dynamic");
//            log.info("MULE-ID: " + context.getConfiguration().getId());
            context.start();
            
            if (context.isStarted()) {
                dynamicContext = context;
            }
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private void remove() {
        if (dynamicContext != null) {
            try {
				dynamicContext.stop();
				dynamicContext.dispose();
			} catch (MuleException e) {
	            throw new RuntimeException(e);
			}
        }
    }

//    private MuleMessage run(String flowName, MuleMessage message) throws MuleException {
//        Flow flow = (Flow)dynamicContext.getRegistry().lookupFlowConstruct(flowName);
//        return flow.process(new DefaultMuleEvent(message, MessageExchangePattern.REQUEST_RESPONSE, new DefaultMuleSession(flow, dynamicContext))).getMessage();
//    }

    private SpringXmlConfigurationBuilder springApplicationBuilderUsing(List<String> payload) {
    	ConfigResource[] resources = createResources(payload);
        SpringXmlConfigurationBuilder springXmlConfigurationBuilder = new SpringXmlConfigurationBuilder(resources);
        springXmlConfigurationBuilder.setParentContext(this.applicationContext);
        return springXmlConfigurationBuilder;
    }

    private ConfigResource[] createResources(List<String> muleConfigs) {

        ConfigResource[] configResources = new ConfigResource[muleConfigs.size()];

        Iterator<String> it = muleConfigs.iterator();
        for (int i=0; it.hasNext(); i++) {
            String muleConfig = it.next();
            configResources[i] = new ConfigResource("context"+i+".xml", new ByteArrayInputStream(muleConfig.getBytes()));
        }

        return configResources;
    }

	/*
	 *	addDynamicContext();
	 *	callDynamicFlow();
	 *
    private MuleContext mainContext;
	public void setMuleContext(MuleContext mainContext) {
		log.info("Initializer.setMuleContext()");
		this.mainContext = mainContext;
	}

	private void addDynamicContext() throws Exception, MuleException {
        Flow flowAdd = lookupFlowConstruct("testAdd");
        MuleEvent eventAdd = AbstractMuleTestCase.getTestEvent(null);
        flowAdd.process(eventAdd);
    }
	
	private void callDynamicFlow() throws Exception, MuleException {
		Flow flow = lookupFlowConstruct("testRun");
		MuleEvent event = AbstractMuleTestCase.getTestEvent("ML-Request");
		MuleEvent responseEvent = flow.process(event);
		log.info("RESPONSE: " + responseEvent.getMessage().getPayload());
	}

	/ **
     * Retrieve a flow by name from the registry
     *
     * @param name Name of the flow to retrieve
     * /
    private Flow lookupFlowConstruct(String name) {
        return (Flow)mainContext.getRegistry().lookupFlowConstruct(name);
    }
    */
    
}