/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.entity.model;

/**
 * The logical business key of an Engagement entity.
 *
 */
public interface BusinessKey {
    /**
     * Returns resident id, i.e. the personal identification.
     * 
     * @return the id, or null if not set
     */
    String getRegisteredResidentIdentification();

    /**
     * Returns service domain.
     * 
     * @return the service domain, or null if not set
     */
    String getServiceDomain();

    /**
     * Returns the categorization.
     * 
     * @return the categorization, or null if not set
     */
    String getCategorization();

    /**
     * Returns logical address.
     * 
     * @return the logical address, or null if not set
     */
    String getLogicalAddress();

    /**
     * Returns business object instance id.
     * 
     * @return the business object instance id, or null if not set
     */
    String getBusinessObjectInstanceIdentifier();

    /**
     * Returns source system.
     * 
     * @return the source system, or null if not set
     */
    String getSourceSystem();

    /**
     * Return clinical process interest id.
     * 
     * @return the id, or null if not set
     */
    String getClinicalProcessInterestId();

    /**
     * Returns the data controller.
     * 
     * @return the data controller, or null if not set.
     */
    String getDataController();

    /**
     * Returns owner of this engagement record.
     * 
     * @return the owner, or null if not set
     */
    String getOwner();

}
