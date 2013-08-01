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
package se.skltp.ei.svc.entity.model;

/**
 * The logical business key of an Engagement entity.
 * 
 * @see Engagement#setBusinessKey(String, String, String, String, String, String, String, String, String)
 * 
 * @author Peter
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
