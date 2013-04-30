package se.skltp.ei.svc.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import se.skltp.ei.svc.entity.model.Engagement;

/**
 * Please note. At this time these methods below are just placeholders and needs further updates as 
 * implementation of use-cases evolves.
 * 
 * @author Peter
 *
 */
public interface EngagementRepository extends JpaRepository<Engagement, String>, JpaSpecificationExecutor<Engagement> {
    /**
     * Returns engagement records matching an array of identities  (primary keys)
     * 
     * @param ids the list of identities.
     * @return the matching list of records.
     */
    List<Engagement> findByIdIn(List<String> ids);

    /**
     * Returns engagements for a particular person.
     * 
     * @param registeredResidentIdentification the identity.
     * @return the list of engagements.
     */
    List<Engagement> findByRegisteredResidentIdentification(String registeredResidentIdentification);
    
}
