/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.entity.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import se.skltp.ei.entity.model.Engagement;

/**
 * Database repository API and specific methods.
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
