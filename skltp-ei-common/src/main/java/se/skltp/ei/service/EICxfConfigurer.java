package se.skltp.ei.service;


import org.apache.camel.component.cxf.jaxws.CxfConfigurer;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.AbstractWSDLBasedEndpointFactory;

import se.skltp.ei.interceptors.SoapActionRemoverInInterceptor;
import se.skltp.ei.service.logging.MessageLoggingFeature;


public class EICxfConfigurer implements CxfConfigurer {
  MessageLoggingFeature messageLoggingFeature;

  public EICxfConfigurer(int maxPayloadSize, String loggerName, String componentId) {
    messageLoggingFeature = new MessageLoggingFeature(maxPayloadSize, loggerName, componentId);
  }

  @Override
  public void configure(AbstractWSDLBasedEndpointFactory factoryBean) {
    addMessageLoggingFeature(factoryBean);
    factoryBean.getInInterceptors().add(new SoapActionRemoverInInterceptor());
  }

  private boolean addMessageLoggingFeature(AbstractWSDLBasedEndpointFactory factoryBean) {
    return factoryBean.getFeatures().add(messageLoggingFeature);
  }

  @Override
  public void configureClient(Client client) {

  }

  @Override
  public void configureServer(Server server) {

  }
}
