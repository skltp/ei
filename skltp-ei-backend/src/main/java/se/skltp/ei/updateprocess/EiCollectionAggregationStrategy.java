package se.skltp.ei.updateprocess;

import static se.skltp.ei.service.util.EntityTransformer.toEntity;

import java.util.Map;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.service.util.JaxbUtil;

@Component
public class EiCollectionAggregationStrategy extends AbstractMapAggregationStrategy<String, EngagementTransactionType>{

  private static JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class);
  private static riv.itintegration.engagementindex.updateresponder._1.ObjectFactory objectFactoryUpdate = new riv.itintegration.engagementindex.updateresponder._1.ObjectFactory();

  @Override
  public Object getCompletedBody(Map<String, EngagementTransactionType> map) {

   if(map.isEmpty()){
     return null;
   }

   UpdateType updateRequest = new UpdateType();
   for( EngagementTransactionType engagementTransactionType : map.values() ){
     updateRequest.getEngagementTransaction().add(engagementTransactionType);
    }

   return jabxUtil.marshal(objectFactoryUpdate.createUpdate(updateRequest));
  }

  @Override
  public Object updateMap( Map<String, EngagementTransactionType> map, Exchange exchange) {

    String message = exchange.getIn().getBody(String.class);
    UpdateType updateRecord = (UpdateType) jabxUtil.unmarshal(message);

    for (final EngagementTransactionType newEngagementTransaction : updateRecord.getEngagementTransaction()) {
      String newHashId = toEntity(newEngagementTransaction.getEngagement()).getId();
      final EngagementTransactionType oldEngagementTransaction = map.get(newHashId);

      if( shouldUpdateMap(newEngagementTransaction, oldEngagementTransaction)){
        map.put(newHashId, newEngagementTransaction);
      }

    }

    return null;
  }


  boolean shouldUpdateMap(final EngagementTransactionType newEngagementTransaction,  final EngagementTransactionType oldEngagementTransaction) {

    if (oldEngagementTransaction == null) {
      return true;
    }
    if (newEngagementTransaction.isDeleteFlag()) {
      return true;
    }
    if (oldEngagementTransaction.isDeleteFlag()) {
      return false;
    }

    return isNewEngagementMoreRecent(newEngagementTransaction.getEngagement(), oldEngagementTransaction.getEngagement());
  }

  private boolean isNewEngagementMoreRecent(EngagementType newEngagementType, EngagementType oldEngagementType) {

    if (newEngagementType.getMostRecentContent() == null) {
      return false;
    }
    if (oldEngagementType.getMostRecentContent() == null) {
      return true;
    }

    Long newDate = Long.parseLong(newEngagementType.getMostRecentContent());
    Long oldDate = Long.parseLong(oldEngagementType.getMostRecentContent());
    return newDate > oldDate;

  }

}
