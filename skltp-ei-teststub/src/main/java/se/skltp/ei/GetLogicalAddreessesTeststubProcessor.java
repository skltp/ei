package se.skltp.ei;

import java.util.ArrayList;
import java.util.List;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.stereotype.Component;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.FilterType;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.GetLogicalAddresseesByServiceContractResponseType;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.GetLogicalAddresseesByServiceContractType;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.LogicalAddresseeRecordType;

@Component
public class GetLogicalAddreessesTeststubProcessor implements Processor {

  @Override
  public void process(Exchange exchange) throws Exception {
    final MessageContentsList messageContentsList = exchange.getIn().getBody(MessageContentsList.class);
    String logicalAddress = (String) messageContentsList.get(0);
    GetLogicalAddresseesByServiceContractType request = (GetLogicalAddresseesByServiceContractType) messageContentsList.get(1);

    exchange.getIn().setBody(createResponse(request));
  }

  // TODO This answer is hardcoded. Create possibility to change it dynamically for easier testing
  private GetLogicalAddresseesByServiceContractResponseType createResponse(GetLogicalAddresseesByServiceContractType request) {
    GetLogicalAddresseesByServiceContractResponseType resp = new GetLogicalAddresseesByServiceContractResponseType();

    List<FilterType> filterTypes = new ArrayList<>();
    filterTypes.add(createFilterType("TEST-DOMAIN", "TEST-CATEGORY", "TEST-CATEGORY"));
    resp.getLogicalAddressRecord().add(createLogicalAddresseeRecordType("TEST-LOGICAL-ADDRESS", filterTypes));

    List<FilterType> filterTypes2 = new ArrayList<>();
    filterTypes2.add(createFilterType("TEST-DOMAIN", "TEST-CATEGORY", "TEST-CATEGORY"));
    filterTypes2.add(createFilterType("TEST-DOMAIN-2", "TEST-CATEGORY", "TEST-CATEGORY-2"));
    resp.getLogicalAddressRecord().add(createLogicalAddresseeRecordType("TEST-LOGICAL-ADDRESS-2", filterTypes2));

    return resp;
  }

  private FilterType createFilterType(String serviceDomain, String... categorizations) {
    final FilterType filterType = new FilterType();
    filterType.setServiceDomain(serviceDomain);
    for (String categorization : categorizations) {
      filterType.getCategorization().add(categorization);
    }
    return filterType;
  }


  private LogicalAddresseeRecordType createLogicalAddresseeRecordType(String logicalAddress, List<FilterType> filters) {
    final LogicalAddresseeRecordType logicalAddresseeRecordType = new LogicalAddresseeRecordType();
    logicalAddresseeRecordType.setLogicalAddress(logicalAddress);
    logicalAddresseeRecordType.getFilter().addAll(filters);
    return logicalAddresseeRecordType;
  }
}
