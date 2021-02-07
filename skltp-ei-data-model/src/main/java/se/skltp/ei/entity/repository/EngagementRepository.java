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
