package se.skltp.ei.service.logging;

import lombok.Data;
import org.apache.cxf.ext.logging.event.LogEvent;

@Data
public class LogEntry {

  protected LogEvent logEvent;

  protected String msgType;

  protected String hostName;

  protected String componentId;

  protected String messageId;
  protected String businessCorrelationId;

  protected String payload;

}

 /*
 2021-03-25 13:45:03,220 INFO  [[skltp-ei-application-mule-frontend-app].soitoolkit-http-connector.receiver.97] org.soitoolkit.commons.mule.messageLogger - soi-toolkit.log
** logEvent-info.start ***********************************************************
IntegrationScenarioId=
ContractId=
LogMessage=req-in
ServiceImpl=update-service
Host=ind-stjp-ei1.ind1.sth.basefarm.net (10.252.7.173)
ComponentId=skltp-ei-application-mule-frontend-app
Endpoint=http://0.0.0.0:8081/skltp-ei/update-service/v1 (POST on /skltp-ei/update-service/v1)
MessageId=eb350340-8d67-11eb-825c-005056a1101d
BusinessCorrelationId=7e18e71c-6f9c-4afd-9163-f7650f456e0b
BusinessContextId=
ExtraInfo=
-originalServiceconsumerHsaid=SE2321000016-A38X
Payload=org.apache.commons.httpclient.ContentLengthInputStream@7510deec
** logEvent-info.end *************************************************************
  */

 /*
 ** logEvent-info.start ***********************************************************
IntegrationScenarioId=
ContractId=
LogMessage=req-out
ServiceImpl=notify-service-SE2321000065-TEST0008
Host=ind-stjp-ei1.ind1.sth.basefarm.net (10.252.7.173)
ComponentId=skltp-ei-application-mule-backend-app
Endpoint=http://ind-stjp-vp-http.ind1.sth.basefarm.net:8080/vp/ProcessNotification/1/rivtabp21 (POST)
MessageId=d4fe4181-8d68-11eb-825c-005056a1101d
BusinessCorrelationId=8f5bdff4-1b04-4259-b93d-cd5a1471d5b9
BusinessContextId=
ExtraInfo=
Payload=org.mule.module.cxf.transport.MuleUniversalConduit$1@768a5409
** logEvent-info.end *************************************************************
  */

 /*
** logEvent-info.start ***********************************************************
IntegrationScenarioId=
ContractId=
LogMessage=resp-in
ServiceImpl=notify-service-SE2321000016-A29F
Host=ind-stjp-ei1.ind1.sth.basefarm.net (10.252.7.173)
ComponentId=skltp-ei-application-mule-backend-app
Endpoint=jms://EI.NOTIFICATION.SE2321000016-A29F
MessageId=f451c6c0-8d67-11eb-825c-005056a1101d
BusinessCorrelationId=
BusinessContextId=
ExtraInfo=
Payload=<?xml version="1.0" encoding="UTF-8" standalone="yes"?><ns8:processNotificationResponseType xmlns="urn:org.soitoolkit.commons.logentry.schema:v1" xmlns:ns6="urn:riv:infrastructure:itintegration:registry:GetLogicalAddresseesByServiceContractResponder:2" xmlns:ns5="urn:riv:itintegration:engagementindex:UpdateResponder:1" xmlns:ns8="class:riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType" xmlns:ns7="urn:riv:infrastructure:itintegration:registry:2" xmlns:ns2="urn:riv:itintegration:engagementindex:1" xmlns:ns4="urn:riv:itintegration:engagementindex:ProcessNotificationResponder:1" xmlns:ns3="urn:riv:itintegration:engagementindex:FindContentResponder:1"><ns4:ResultCode>OK</ns4:ResultCode></ns8:processNotificationResponseType>
  */

/*
** logEvent-info.start ***********************************************************
IntegrationScenarioId=
ContractId=
LogMessage=resp-out
ServiceImpl=update-service
Host=ind-stjp-ei1.ind1.sth.basefarm.net (10.252.7.173)
ComponentId=skltp-ei-application-mule-frontend-app
Endpoint=http://0.0.0.0:8081/skltp-ei/update-service/v1 (POST on /skltp-ei/update-service/v1)
MessageId=eb350340-8d67-11eb-825c-005056a1101d
BusinessCorrelationId=7e18e71c-6f9c-4afd-9163-f7650f456e0b
BusinessContextId=
ExtraInfo=
Payload=org.mule.module.cxf.CxfInboundMessageProcessor$2@6a77aec1
 */


/*
 ** logEvent-info.start ***********************************************************
IntegrationScenarioId=
ContractId=
LogMessage=msg-in
ServiceImpl=process-service
Host=ind-stjp-ei2.ind1.sth.basefarm.net (10.252.7.174)
ComponentId=skltp-ei-application-mule-backend-app
Endpoint=jms://skltp.ei.process
MessageId=27463d21-8d65-11eb-89a9-005056a1400d
BusinessCorrelationId=56c56485-4499-43b9-a28d-26184a16fff6
BusinessContextId=
ExtraInfo=
-nrRecords=1
-msgType=update
-collectNrMsgs=1
-collectNrRecords=1
-collectBufAgeMs=15002
-originalServiceconsumerHsaid=collect
-isViaCollect=true
Payload=<?xml version="1.0" encoding="UTF-8" standalone="yes"?><ns2:Update xmlns="urn:riv:itintegration:engagementindex:1" xmlns:ns2="urn:riv:itintegration:engagementindex:UpdateResponder:1"><ns2:engagementTransaction><deleteFlag>false</deleteFlag><engagement><registeredResidentIdentification>194901262172</registeredResidentIdentification><serviceDomain>riv:clinicalprocess:logistics:logistics</serviceDomain><categorization>vko</categorization><logicalAddress>SE2321000057-4LPF</logicalAddress><businessObjectInstanceIdentifier>NA</businessObjectInstanceIdentifier><clinicalProcessInterestId>NA</clinicalProcessInterestId><mostRecentContent>20210325132139</mostRecentContent><sourceSystem>SE2321000057-4LPF</sourceSystem><dataController>SE2321000057-493K</dataController><owner>5565594230</owner></engagement></ns2:engagementTransaction></ns2:Update>
** logEvent-info.end *************************************************************
}

/*
** logEvent-info.start ***********************************************************
IntegrationScenarioId=
ContractId=
LogMessage=msg-out
ServiceImpl=update-service
Host=ind-stjp-ei1.ind1.sth.basefarm.net (10.252.7.173)
ComponentId=skltp-ei-application-mule-frontend-app
Endpoint=jms://skltp.ei.collect
MessageId=90eabc30-8d68-11eb-825c-005056a1101d
BusinessCorrelationId=ba799ee8-14d4-4251-bbe2-6e31ba1629e5
BusinessContextId=
ExtraInfo=
-nrRecords=1
-originalServiceconsumerHsaid=HSATEST2-CMH
-isViaCollect=true
Payload=<?xml version="1.0" encoding="UTF-8" standalone="yes"?><ns2:Update xmlns="urn:riv:itintegration:engagementindex:1" xmlns:ns2="urn:riv:itintegration:engagementindex:UpdateResponder:1"><ns2:engagementTransaction><deleteFlag>false</deleteFlag><engagement><registeredResidentIdentification>191212121212</registeredResidentIdentification><serviceDomain>riv:clinicalprocess:healthcond:actoutcome</serviceDomain><categorization>und-kkm-ure</categorization><logicalAddress>HSATEST2-D2V</logicalAddress><businessObjectInstanceIdentifier>NA</businessObjectInstanceIdentifier><clinicalProcessInterestId>NA</clinicalProcessInterestId><mostRecentContent>20210325133100</mostRecentContent><sourceSystem>HSATEST2-D2V</sourceSystem><dataController>5565594230</dataController><owner>5565594230</owner></engagement></ns2:engagementTransaction></ns2:Update>
** logEvent-info.end *************************************************************
 */
