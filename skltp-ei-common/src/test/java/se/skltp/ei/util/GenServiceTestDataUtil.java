
package se.skltp.ei.util;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import se.skltp.ei.entity.model.Engagement;
import se.skltp.ei.service.util.EntityTransformer;

public class GenServiceTestDataUtil {
	
   public static EngagementTransactionType generateEngagementTransaction(long residentId) {
		Engagement entity = GenEntityTestDataUtil.generateEngagement(residentId);
		EngagementType engagement = EntityTransformer.fromEntity(entity);
    	EngagementTransactionType et = new EngagementTransactionType();
    	et.setDeleteFlag(false);
    	et.setEngagement(engagement);
		return et;
	}

}