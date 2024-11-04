package se.skltp.ei;

import java.util.ArrayList;
import java.util.List;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.stereotype.Component;
import riv.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._2.FilterType;
import riv.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._2.GetLogicalAddresseesByServiceContractResponseType;
import riv.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._2.GetLogicalAddresseesByServiceContractType;
import riv.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._2.LogicalAddresseeRecordType;

@Component
public class GetLogicalAddreessesTeststubProcessor implements Processor {

  public static final String TEST_CATEGORY = "TEST-CATEGORY";
  public static final String TEST_CATEGORY_2 = "TEST-CATEGORY-2";
  public static final String TEST_DOMAIN = "TEST-DOMAIN";
  public static final String TEST_DOMAIN_2 = "TEST-DOMAIN-2";
  public static final String TEST_LOGICAL_ADDRESS = "TEST-LOGICAL-ADDRESS";
  public static final String TEST_LOGICAL_ADDRESS_2 = "TEST-LOGICAL-ADDRESS-2";

  @Override
  public void process(Exchange exchange) throws Exception {
    final MessageContentsList messageContentsList = exchange.getIn().getBody(MessageContentsList.class);
    GetLogicalAddresseesByServiceContractType request = (GetLogicalAddresseesByServiceContractType) messageContentsList.get(1);

    exchange.getIn().setBody(createResponse());
  }

  private GetLogicalAddresseesByServiceContractResponseType createResponse() {
    GetLogicalAddresseesByServiceContractResponseType resp = new GetLogicalAddresseesByServiceContractResponseType();

    List<FilterType> filterTypes = new ArrayList<>();
    filterTypes.add(createFilterType(TEST_DOMAIN, TEST_CATEGORY, TEST_CATEGORY_2));
    resp.getLogicalAddressRecord().add(createLogicalAddresseeRecordType(TEST_LOGICAL_ADDRESS, filterTypes));

    List<FilterType> filterTypes2 = new ArrayList<>();
    filterTypes2.add(createFilterType(TEST_DOMAIN, TEST_CATEGORY, TEST_CATEGORY_2));
    filterTypes2.add(createFilterType(TEST_DOMAIN_2, TEST_CATEGORY, TEST_CATEGORY_2));
    resp.getLogicalAddressRecord().add(createLogicalAddresseeRecordType(TEST_LOGICAL_ADDRESS_2, filterTypes2));

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
