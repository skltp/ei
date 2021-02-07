package se.skltp.ei.update;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.stereotype.Service;
import riv.itintegration.engagementindex.updateresponder._1.ObjectFactory;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.service.constants.EiConstants;
import se.skltp.ei.service.util.JaxbUtil;

@Service
public class UpdateRequestToJmsMessageProcessor implements Processor {

  private static JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class);
  private static ObjectFactory of = new ObjectFactory();

  @Override
  public void process(Exchange exchange) throws Exception {
    MessageContentsList updateContentsList = exchange.getIn().getBody(MessageContentsList.class);

    UpdateType updateType = (UpdateType) updateContentsList.get(1);
    String jmsMsg = jabxUtil.marshal(of.createUpdate(updateType));
    exchange.getIn().setBody(jmsMsg);

    exchange.setProperty(EiConstants.LOGICAL_ADRESS, updateContentsList.get(0));
    exchange.setProperty(EiConstants.EI_LOG_NUMBER_OF_RECORDS_IN_MESSAGE, updateType.getEngagementTransaction().size());
    exchange.setProperty(EiConstants.EI_LOG_MESSAGE_TYPE, EiConstants.EI_LOG_MESSAGE_TYPE_UPDATE);

  }
}
