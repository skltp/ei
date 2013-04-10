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
	    
		eOut.setRegisteredResidentIdentification(eIn.getRegisteredResidentIdentification());
	    eOut.setServiceDomain(eIn.getServiceDomain());
	    eOut.setCategorization(eIn.getCategorization());
	    eOut.setLogicalAddress(eIn.getLogicalAddress());
	    eOut.setBusinessObjectInstanceIdentifier(eIn.getBusinessObjectInstanceIdentifier());
	    eOut.setSourceSystem(eIn.getSourceSystem());
// FIXME	    eOut.setCreationTime(eIn.getCreationTime());
	    eOut.setOwner(eIn.getOwner());
// FIXME	    eOut.setUpdateTime(eIn.getUpdateTime());
	    eOut.setClinicalProcessInterestId(eIn.getClinicalProcessInterestId());
		
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
		
	    eOut.setRegisteredResidentIdentification(eIn.getRegisteredResidentIdentification());
	    eOut.setServiceDomain(eIn.getServiceDomain());
	    eOut.setCategorization(eIn.getCategorization());
	    eOut.setLogicalAddress(eIn.getLogicalAddress());
	    eOut.setBusinessObjectInstanceIdentifier(eIn.getBusinessObjectInstanceIdentifier());
	    eOut.setSourceSystem(eIn.getSourceSystem());
// FIXME	    eOut.setCreationTime(eIn.getCreationTime());
	    eOut.setOwner(eIn.getOwner());
// FIXME	    eOut.setUpdateTime(eIn.getUpdateTime());
	    eOut.setClinicalProcessInterestId(eIn.getClinicalProcessInterestId());
		
	    return eOut;
	}

}
