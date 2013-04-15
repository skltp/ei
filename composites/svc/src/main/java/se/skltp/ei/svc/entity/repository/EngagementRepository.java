package se.skltp.ei.svc.entity.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import se.skltp.ei.svc.entity.model.Engagement;

//
public interface EngagementRepository extends CrudRepository<Engagement, String> {
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
    List<Engagement> findByBusinessKey_RegisteredResidentIdentification(String registeredResidentIdentification);
}
