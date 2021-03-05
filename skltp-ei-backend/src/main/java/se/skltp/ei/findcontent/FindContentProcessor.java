package se.skltp.ei.findcontent;

import static se.skltp.ei.service.api.EiErrorCodeEnum.EI000_TECHNICAL_ERROR;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;
import se.skltp.ei.service.FindContentService;

import static se.skltp.ei.service.util.EIUtils.isEmpty;

@Component
public class FindContentProcessor implements Processor {

  @Autowired
  FindContentService findContentService;

  public static final String MISSING_PERSON_MESSAGE = "registeredResidentIdentification is mandatory but missing";
  public static final String MISSING_SERVICEDOMAIN_MESSAGE = "serviceDomain is mandatory but missing";

  @Override
  public void process(Exchange exchange) throws Exception {
    MessageContentsList contentsList = exchange.getIn().getBody(MessageContentsList.class);
    FindContentType findContent = (FindContentType) contentsList.get(1);

    validateFindContent(findContent);

    final FindContentResponseType findContentResponse = findContentService.findContent(findContent);
    exchange.getIn().setBody(findContentResponse);
  }

  private void validateFindContent(FindContentType findContent) {
    // We need at least registeredResidentIdentification and serviceDomain to do a query
    if(isEmpty(findContent.getRegisteredResidentIdentification())) {
      throw EI000_TECHNICAL_ERROR.createException(MISSING_PERSON_MESSAGE);

    } else if(isEmpty(findContent.getServiceDomain())) {
      throw EI000_TECHNICAL_ERROR.createException(MISSING_SERVICEDOMAIN_MESSAGE);
    }
  }

}
