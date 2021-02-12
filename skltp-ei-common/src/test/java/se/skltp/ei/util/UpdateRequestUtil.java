package se.skltp.ei.util;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.updateresponder._1.ObjectFactory;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.service.util.JaxbUtil;
import se.skltp.ei.util.EngagementTestUtil.DomainType;

public class UpdateRequestUtil {

  private static final JaxbUtil jaxbUtil = new JaxbUtil(UpdateType.class);
  private static final ObjectFactory update_of = new ObjectFactory();


  public static UpdateType createUpdate(String owner, long... residentIds) {

    UpdateType request = new UpdateType();

    for (int i = 0; i < residentIds.length; i++) {
      EngagementTransactionType et = EngagementTransactionTestUtil.createET(residentIds[i]);
      request.getEngagementTransaction().add(et);
      et.getEngagement().setOwner(owner);
    }

    return request;
  }


  public static UpdateType createUpdate(String owner, DomainType domainType, long... residentIds) {

    UpdateType request = new UpdateType();

    for (int i = 0; i < residentIds.length; i++) {
      EngagementTransactionType et = EngagementTransactionTestUtil.createET(residentIds[i], domainType);
      request.getEngagementTransaction().add(et);
      et.getEngagement().setOwner(owner);
    }

    return request;
  }

  public static UpdateType createUpdate(String owner, EngagementTransactionType... etList) {

    UpdateType request = new UpdateType();

    for (EngagementTransactionType et : etList) {
      request.getEngagementTransaction().add(et);
      et.getEngagement().setOwner(owner);
    }
    return request;
  }



  public static String createUpdateTxtMsg(String owner, long... residentIds) {
    return jaxbUtil.marshal(update_of.createUpdate(createUpdate(owner, residentIds)));
  }

  public static String createUpdateTxtMsg(String owner, DomainType domainType, long... residentIds) {
    return jaxbUtil.marshal(update_of.createUpdate(createUpdate(owner, domainType, residentIds)));
  }

  public static String createUpdateTxtMsg(String owner, EngagementTransactionType... etList) {
    return jaxbUtil.marshal(update_of.createUpdate(createUpdate(owner, etList)));
  }


}
