/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import riv.infrastructure.itintegration.registry._2.ServiceContractNamespaceType;
import riv.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontract._2.rivtabp21.GetLogicalAddresseesByServiceContractResponderInterface;
import riv.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontract._2.rivtabp21.GetLogicalAddresseesByServiceContractResponderService;
import riv.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._2.GetLogicalAddresseesByServiceContractResponseType;
import riv.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._2.GetLogicalAddresseesByServiceContractType;

import se.skltp.ei.service.config.GetLogicalAddressesConfig;

@Log4j2
@Service
public class GetLogicalAddresseesServiceClient {


  private static final String HTTP_HEADER_VP_SENDER_ID = "x-vp-sender-id";
  private static final String HTTP_HEADER_VP_INSTANCE_ID = "x-vp-instance-id";
  private static final String PROCESS_NOTIFICATION_SERVICE_CONTRACT_NAMESPACE = "urn:riv:itintegration:engagementindex:ProcessNotificationResponder:1";
  private static final String WSDL_DEFINITION_NAME = "GetLogicalAddresseesByServiceContractInteraction";
  private static final String WSDL_NAMESPACE = "urn:riv:infrastructure:itintegration:registry:GetLogicalAddresseesByServiceContract:2:rivtabp21";

  @Autowired
  GetLogicalAddressesConfig getLogicalAddressesConfig;


  public GetLogicalAddresseesByServiceContractResponseType callService()  {
    log.info("invoking remote service, url: {}, logicalAddress: {}, vpSenderId: {}, vpInstanceId: {}",
        getLogicalAddressesConfig.getServiceEndpointUrl(), getLogicalAddressesConfig.getLogicalAddress(), getLogicalAddressesConfig
            .getVpSenderId(), getLogicalAddressesConfig.getVpInstanceId());

    GetLogicalAddresseesByServiceContractResponseType response = callService(buildRequest());

    log.info("response from remote service contains #records: {}", response
        .getLogicalAddressRecord().size());
    return response;
  }

  private GetLogicalAddresseesByServiceContractType buildRequest() {
    GetLogicalAddresseesByServiceContractType req = new GetLogicalAddresseesByServiceContractType();
    req.setServiceConsumerHsaId(getLogicalAddressesConfig.getLogicalAddress());
    ServiceContractNamespaceType ns = new ServiceContractNamespaceType();
    ns.setServiceContractNamespace(PROCESS_NOTIFICATION_SERVICE_CONTRACT_NAMESPACE);
    req.setServiceContractNameSpace(ns);
    return req;
  }


  private GetLogicalAddresseesByServiceContractResponseType callService(
      GetLogicalAddresseesByServiceContractType request) {

    GetLogicalAddresseesByServiceContractResponderService svc = new GetLogicalAddresseesByServiceContractResponderService(
        null, new QName(WSDL_NAMESPACE, WSDL_DEFINITION_NAME));

    GetLogicalAddresseesByServiceContractResponderInterface port = svc.getGetLogicalAddresseesByServiceContractResponderPort();
    // set endpoint
		Map<String, Object> reqCtx = ((BindingProvider) port).getRequestContext();
		reqCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getLogicalAddressesConfig.getServiceEndpointUrl());
		reqCtx.put(MessageContext.HTTP_REQUEST_HEADERS, getHttpHeaders());

		setTimeoutsForMultipleJaxWsImplementations(reqCtx);

    return port.getLogicalAddresseesByServiceContract(getLogicalAddressesConfig.getLogicalAddress(), request);
  }


  private Map<String, List<String>> getHttpHeaders() {
    Map<String, List<String>> httpHeaders = new HashMap<>();
    httpHeaders.put(HTTP_HEADER_VP_INSTANCE_ID, Collections.singletonList(getLogicalAddressesConfig.getVpInstanceId()));
    httpHeaders.put(HTTP_HEADER_VP_SENDER_ID, Collections.singletonList(getLogicalAddressesConfig.getVpSenderId()));
    return httpHeaders;
  }

  /**
   * Note: timeout properties are not standardized, depends on JAX-WS implementation.
   */
  private void setTimeoutsForMultipleJaxWsImplementations(Map<String, Object> reqCtx ) {
    // JDK JAX-WS
		reqCtx.put("com.sun.xml.internal.ws.connect.timeout", getLogicalAddressesConfig.getConnectTimeoutMs());
		reqCtx.put("com.sun.xml.internal.ws.request.timeout", getLogicalAddressesConfig.getRequestTimeoutMs());
    // CXF
		reqCtx.put("javax.xml.ws.client.connectionTimeout", getLogicalAddressesConfig.getConnectTimeoutMs());
		reqCtx.put("javax.xml.ws.client.receiveTimeout", getLogicalAddressesConfig.getRequestTimeoutMs());
  }
}
