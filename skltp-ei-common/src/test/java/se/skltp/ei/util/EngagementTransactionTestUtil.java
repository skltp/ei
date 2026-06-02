/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */

package se.skltp.ei.util;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import se.skltp.ei.entity.model.Engagement;
import se.skltp.ei.service.util.EntityTransformer;
import se.skltp.ei.util.EngagementTestUtil.DomainType;

public class EngagementTransactionTestUtil {
	
   public static EngagementTransactionType createET(long residentId) {
		Engagement entity = EngagementTestUtil.generateEngagement(residentId);
		EngagementType engagement = EntityTransformer.fromEntity(entity);
    	EngagementTransactionType et = new EngagementTransactionType();
    	et.setDeleteFlag(false);
    	et.setEngagement(engagement);
		return et;
	}

	public static EngagementTransactionType createET(long residentId, DomainType domainType) {
		Engagement entity = EngagementTestUtil.generateEngagement(residentId, domainType);
		EngagementType engagement = EntityTransformer.fromEntity(entity);
		EngagementTransactionType et = new EngagementTransactionType();
		et.setDeleteFlag(false);
		et.setEngagement(engagement);
		return et;
	}

	public static EngagementTransactionType createET( DomainType domainType, long residentIds, boolean delete) {
		EngagementTransactionType et = createET(residentIds, domainType);
		et.setDeleteFlag(delete);
		return et;
	}

}