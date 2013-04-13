package se.skltp.ei.svc.service.impl.util;

import riv.itintegration.engagementindex._1.EngagementType;
import se.skltp.ei.svc.entity.model.Engagement;

public class EntityTransformer {

	/**
	 * Transform an engagement from the service model to the entity model
	 * 
	 * @param eIn
	 * @return eOut
	 */
	public static Engagement toEntity(EngagementType eIn) {
		
		Engagement eOut = new Engagement();
		
		eOut.setBusinessKey(eIn.getRegisteredResidentIdentification(),
				eIn.getServiceDomain(),
				eIn.getCategorization(),
				eIn.getLogicalAddress(),
				eIn.getBusinessObjectInstanceIdentifier(),
				eIn.getSourceSystem(),
				eIn.getOwner(),
				eIn.getClinicalProcessInterestId());
				
// FIXME	    eOut.setCreationTime(eIn.getCreationTime());
// FIXME	    eOut.setUpdateTime(eIn.getUpdateTime());
		
	    return eOut;
	}

	/**
	 * Transform an engagement from the entity model to the service model
	 * 
	 * @param eIn
	 * @return eOut
	 */
	public static EngagementType fromEntity(Engagement eIn) {
		
		EngagementType eOut = new EngagementType();
		Engagement.BusinessKey key = eIn.getBusinessKey();
	    eOut.setRegisteredResidentIdentification(key.getRegisteredResidentIdentification());
	    eOut.setServiceDomain(key.getServiceDomain());
	    eOut.setCategorization(key.getCategorization());
	    eOut.setLogicalAddress(key.getLogicalAddress());
	    eOut.setBusinessObjectInstanceIdentifier(key.getBusinessObjectInstanceIdentifier());
	    eOut.setSourceSystem(key.getSourceSystem());
// FIXME	    eOut.setCreationTime(eIn.getCreationTime());
	    eOut.setOwner(key.getOwner());
// FIXME	    eOut.setUpdateTime(eIn.getUpdateTime());
	    eOut.setClinicalProcessInterestId(key.getClinicalProcessInterestId());
		
	    return eOut;
	}

}
