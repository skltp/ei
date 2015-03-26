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
package se.skltp.ei.svc.service.api;

import java.util.List;

import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.model.Engagement;

/**
 * Service model API for process notification and update requests.
 * 
 * @author Magnus Larsson
 */
public interface ProcessInterface {

    /**
     * Defines the maximum number of allowed engagement records in a request.
     */
    int MAX_NUMBER_OF_ENGAGEMENTS = 1000;
    
    /**
     * Validates an update request without having to touch the database.
     * 
     * @param header the header
     * @param parameters the update request parameters
     * @throws EiException on validation errors
     */
    void validateUpdate(Header header, UpdateType parameters); 

    /**
     * Performs an engagement index database update due to a an update request. <p>
     * 
     * On any type of error a {@link RuntimeException} shall be expected.
     * 
     * @param header the header
     * @param parameters the update request parameters
     *
	 * @return a list of engagements that should be sent to subscribers
     */
    List<Engagement> update(Header header, UpdateType parameters); 

    /**
     * Validates a process notification request without having to touch the database.
     * 
     * @param header the header
     * @param parameters the update request parameters
     * @throws EiException on validation errors
     */
    void validateProcessNotification(Header header, ProcessNotificationType parameters); 

    /**
     * Performs an engagement index database update due to a process notification request. <p>
     * 
     * On any type of error a {@link RuntimeException} shall be expected.
     * 
     * @param header the header
     * @param parameters the process notification response, which always is OK
	 *
	 * @return a list of engagements that should be sent to subscribers
     */
    List<Engagement> processNotification(Header header, ProcessNotificationType parameters);

    /**
     * Filters out engagement records with the same owner (origin) as this instance, 
     * i.e. avoids never ending recursion of process notification updates.
     * 
     * @param parameters the request parameters.
     * @return the input request parameters.
     */
    ProcessNotificationType filterProcessNotification(ProcessNotificationType parameters);
}
