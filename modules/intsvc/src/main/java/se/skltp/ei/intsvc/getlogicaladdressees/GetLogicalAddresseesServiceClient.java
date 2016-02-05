/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.intsvc.getlogicaladdressees;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontract.v2.rivtabp21.GetLogicalAddresseesByServiceContractResponderInterface;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontract.v2.rivtabp21.GetLogicalAddresseesByServiceContractResponderService;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.GetLogicalAddresseesByServiceContractResponseType;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.GetLogicalAddresseesByServiceContractType;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.LogicalAddresseeRecordType;
import se.rivta.infrastructure.itintegration.registry.v2.ServiceContractNamespaceType;

/**
 * Client wrapping remote call details for the
 * GetLogicalAddresseesByServiceContractInteraction.
 * 
 * @author hakan
 */
public class GetLogicalAddresseesServiceClient {

	private static final Logger log = LoggerFactory
			.getLogger(GetLogicalAddresseesServiceClient.class);

	private static final String HTTP_HEADER_VP_SENDER_ID = "x-vp-sender-id";
	private static final String HTTP_HEADER_VP_INSTANCE_ID = "x-vp-instance-id";
	private static final String PROCESS_NOTIFICATION_SERVICE_CONTRACT_NAMESPACE = "urn:riv:itintegration:engagementindex:ProcessNotificationResponder:1";
	private static final String WSDL_DEFINITION_NAME = "GetLogicalAddresseesByServiceContractInteraction";
	private static final String WSDL_NAMESPACE = "urn:riv:infrastructure:itintegration:registry:GetLogicalAddresseesByServiceContract:2:rivtabp21";

	private String logicalAddress;
	private String vpSenderId;
	private String vpInstanceId;
	private String serviceEndpointUrl;
	private String connectTimeoutMs;
	private String requestTimeoutMs;

	// MANUAL TEST ONLY
	public static void main(String[] args) {
		try {
			GetLogicalAddresseesServiceClient client = new GetLogicalAddresseesServiceClient();
			client.setLogicalAddress("vp-hsa-id");
			client.setVpSenderId("ei-hsa-id");
			client.setVpInstanceId("DEFAULT_NOT_SET");
			client.setServiceEndpointUrl("http://localhost:8083/skltp-ei/get-logical-addressees-by-service-contract-teststub-service/v1");
			GetLogicalAddresseesByServiceContractResponseType resp = client
					.callService();
			for (LogicalAddresseeRecordType lar : resp
					.getLogicalAddressRecord()) {
				log.info("found logicalAddress: {}", lar.getLogicalAddress());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setLogicalAddress(String logicalAddress) {
		this.logicalAddress = logicalAddress;
	}

	public void setVpSenderId(String vpSenderId) {
		this.vpSenderId = vpSenderId;
	}

	public void setVpInstanceId(String vpInstanceId) {
		this.vpInstanceId = vpInstanceId;
	}

	public void setServiceEndpointUrl(String serviceEndpointUrl) {
		this.serviceEndpointUrl = serviceEndpointUrl;
	}

	public void setRequestTimeoutMs(String requestTimeoutMs) {
		this.requestTimeoutMs = requestTimeoutMs;
	}

	public void setConnectTimeoutMs(String connectTimeoutMs) {
		this.connectTimeoutMs = connectTimeoutMs;
	}

	public GetLogicalAddresseesByServiceContractResponseType callService()
			throws IOException {
		if (log.isInfoEnabled()) {
			log.info(
					"invoking remote service, url: {}, logicalAddress: {}, vpSenderId: {}, vpInstanceId: {}",
					new Object[] { serviceEndpointUrl, logicalAddress,
							vpSenderId, vpInstanceId });
		}
		GetLogicalAddresseesByServiceContractResponseType resp = callService(buildRequest());
		log.info("response from remote service contains #records: {}", resp
				.getLogicalAddressRecord().size());
		return resp;
	}

	private GetLogicalAddresseesByServiceContractType buildRequest() {
		GetLogicalAddresseesByServiceContractType req = new GetLogicalAddresseesByServiceContractType();
		req.setServiceConsumerHsaId(logicalAddress);
		ServiceContractNamespaceType ns = new ServiceContractNamespaceType();
		ns.setServiceContractNamespace(PROCESS_NOTIFICATION_SERVICE_CONTRACT_NAMESPACE);
		req.setServiceContractNameSpace(ns);
		return req;
	}

	private GetLogicalAddresseesByServiceContractResponseType callService(
			GetLogicalAddresseesByServiceContractType request)
			throws MalformedURLException {

		// SKLTP-807: backwards compatibility note:
		// do NOT rely on WSDL-lookup here, i.e. do not use the naive JAX-WS
		// way, we need to use the BindingProvider like below to not have
		// JAX-WS do a WSDL-lookup before the service invocation
		GetLogicalAddresseesByServiceContractResponderService svc = new GetLogicalAddresseesByServiceContractResponderService(
				null, new QName(WSDL_NAMESPACE, WSDL_DEFINITION_NAME));
		GetLogicalAddresseesByServiceContractResponderInterface port = svc
				.getGetLogicalAddresseesByServiceContractResponderPort();
		// set endpoint
		BindingProvider bindingProvider = (BindingProvider) port;
		bindingProvider.getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceEndpointUrl);
		// set HTTP-headers
		bindingProvider.getRequestContext().put(
				MessageContext.HTTP_REQUEST_HEADERS,
				getHttpHeadersForJaxWsUsageWithBindingProvider());
		// set timeout properties
		setTimeoutsForMultipleJaxWsImplementations(bindingProvider);

		GetLogicalAddresseesByServiceContractResponseType response = port
				.getLogicalAddresseesByServiceContract(logicalAddress, request);

		return response;
	}

	private Map<String, List<String>> getHttpHeadersForJaxWsUsageWithBindingProvider() {
		Map<String, List<String>> httpHeaders = new HashMap<String, List<String>>();
		{
			List<String> vpInstanceIdValues = new ArrayList<String>();
			vpInstanceIdValues.add(vpInstanceId);
			httpHeaders.put(HTTP_HEADER_VP_INSTANCE_ID, vpInstanceIdValues);
		}
		{
			List<String> vpSenderIdValues = new ArrayList<String>();
			vpSenderIdValues.add(vpSenderId);
			httpHeaders.put(HTTP_HEADER_VP_SENDER_ID, vpSenderIdValues);
		}
		return httpHeaders;
	}

	/**
	 * Note: timeout properties are not standardized, depends on JAX-WS
	 * implementation.
	 * <p>
	 * Ref:
	 * </p>
	 * <ul>
	 * <li>https://java.net/jira/browse/JAX_WS-1166</li>
	 * <li>https://issues.apache.org/jira/browse/CXF-2991</li>
	 * <li>https://metro.java.net/guide/ch05.html#http-timeouts</li>
	 * </ul>
	 * 
	 * @param bindingProvider
	 */
	private void setTimeoutsForMultipleJaxWsImplementations(
			BindingProvider bindingProvider) {
		// JDK JAX-WS
		bindingProvider.getRequestContext().put(
				"com.sun.xml.internal.ws.connect.timeout", connectTimeoutMs);
		bindingProvider.getRequestContext().put(
				"com.sun.xml.internal.ws.request.timeout", requestTimeoutMs);
		// CXF
		bindingProvider.getRequestContext().put(
				"javax.xml.ws.client.connectionTimeout", connectTimeoutMs);
		bindingProvider.getRequestContext().put(
				"javax.xml.ws.client.receiveTimeout", requestTimeoutMs);
	}
}
