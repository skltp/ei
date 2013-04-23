package se.skltp.ei.svc.service;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import se.skltp.ei.svc.entity.GenEntityTestDataUtil;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.service.impl.util.EntityTransformer;

public class GenServiceTestDataUtil {
	
    /**
     * Generates a EngagementTransaction, which is completely derived from the value of residentIdentification (repeatable).
     * 
     * @param e the engagement
     * @return the generated engagement transaction
     */
	public static EngagementTransactionType genEngagementTransaction(long residentId) {
		Engagement entity = GenEntityTestDataUtil.genEngagement(residentId);
		EngagementType engagement = EntityTransformer.fromEntity(entity);
    	EngagementTransactionType et = new EngagementTransactionType();
    	et.setDeleteFlag(false);
    	et.setEngagement(engagement);
		return et;
	}

}