package se.skltp.ei.svc.entity.model;

/**
 * The logical business key.
 * 
 * 
 * @author Peter
 *
 */
public interface BusinessKey {
    String getRegisteredResidentIdentification();

    String getServiceDomain();
        
    String getCategorization();

    String getLogicalAddress();
        
    String getBusinessObjectInstanceIdentifier();
 
    String getSourceSystem();

    String getClinicalProcessInterestId();

    String getDataController();
    
    String getOwner();
}
