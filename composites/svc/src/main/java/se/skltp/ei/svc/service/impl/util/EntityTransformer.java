package se.skltp.ei.svc.service.impl.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import riv.itintegration.engagementindex._1.EngagementType;
import se.skltp.ei.svc.entity.model.Engagement;

public class EntityTransformer {

    static SimpleDateFormat dateFormatter;

    static {
        dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
        // strict syntax mode
        dateFormatter.setLenient(false);
    }

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

        eOut.setCreationTime(parseDate(eIn.getCreationTime()));
        eOut.setUpdateTime(parseDate(eIn.getUpdateTime()));
        eOut.setMostRecentContent(parseDate(eIn.getMostRecentContent()));

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
        eOut.setOwner(key.getOwner());
        eOut.setClinicalProcessInterestId(key.getClinicalProcessInterestId());

        eOut.setCreationTime(forrmatDate(eIn.getCreationTime()));
        eOut.setMostRecentContent(forrmatDate(eIn.getMostRecentContent()));	    
        eOut.setUpdateTime(forrmatDate(eIn.getUpdateTime()));

        return eOut;
    }

    /**
     * Returns a formatted date according to the service contract format.
     * 
     * @param date the date to format.
     * @return the formatted date.
     */
    public static String forrmatDate(Date date) {
        return (date == null) ? null : dateFormatter.format(date);
    }

    /**
     * Returns a date.
     * 
     * @param date the date in text.
     * @return the date.
     */
    public static Date parseDate(String date) {
        try {
            return (date == null) ? null : dateFormatter.parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Input date is not according to expected format \"YYYYMMDDhhmmss\": " + date, e);
        }
    }

}
