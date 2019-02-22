/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
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
	 * @param entity the engagement
	 * @return the generated engagement transaction
	 */
	private static EngagementTransactionType internalEngagementTransaction(Engagement entity) {
		EngagementType engagement = EntityTransformer.fromEntity(entity);
		EngagementTransactionType et = new EngagementTransactionType();
		et.setDeleteFlag(false);
		et.setEngagement(engagement);
		return et;
	}

    /**
     * Generates a EngagementTransaction, which is completely derived from the value of residentIdentification (repeatable).
     * 
     * @param residentId the engagement id
     * @return the generated engagement transaction
     */
	public static EngagementTransactionType genEngagementTransaction(long residentId) {
		Engagement entity = GenEntityTestDataUtil.genEngagement(residentId);
		return internalEngagementTransaction(entity);
	}

	/**
	 *
	 * @param residentId
	 * @param pOwner
	 * @return
	 */
	public static EngagementTransactionType genEngagementTransaction(long residentId,String pOwner) {
		Engagement entity = GenEntityTestDataUtil.genEngagement(residentId,pOwner);
		return internalEngagementTransaction(entity);
	}

}