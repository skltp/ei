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

import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

public interface ProcessInterface {

    /**
     *
     * @param header
     * @param parameters
     */
    public void validateUpdate(Header header, UpdateType parameters); 

    /**
     *
     * @param header
     * @param parameters
     * @return
     */
    public UpdateResponseType update(Header header, UpdateType parameters); 

    /**
    *
    * @param header
    * @param parameters
    */
	public void validateProcessNotification(Header header, ProcessNotificationType parameters); 

    /**
    *
    * @param header
    * @param parameters
    * @return
    */
	public ProcessNotificationResponseType processNotification(Header header, ProcessNotificationType parameters);
	
	/**
	 * Filter away all engagements that has the same owner as the index.
	 * @param  parameters
	 * @return 
	 */
	public ProcessNotificationType filterProcessNotification(ProcessNotificationType parameters);
}
