package se.skltp.ei.updateprocess;

import java.util.List;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.service.UpdatePersistentStorageService;
import se.skltp.ei.service.util.JaxbUtil;

@Component
public class UpdatePersistentStorageProcessor implements Processor {

  @Autowired
  UpdatePersistentStorageService updateDatebaseService;

  private static JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class, ProcessNotificationType.class);

  @Override
  public void process(Exchange exchange) throws Exception {

    Object requestJaxb = jabxUtil.unmarshal(exchange.getIn().getBody());
    List<EngagementTransactionType> engagementsChangedList;
    if (requestJaxb instanceof UpdateType) {
      engagementsChangedList = updateDatebaseService.update((UpdateType)requestJaxb);
    } else {
      engagementsChangedList = updateDatebaseService.update((ProcessNotificationType)requestJaxb);
    }

    exchange.getIn().setBody(engagementsChangedList);
  }
}
