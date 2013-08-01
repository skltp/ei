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

/**
 * Keeps request header parameters of interest, typically used for logging and traceability.
 * 
 * @author Magnus Larsson
 *
 */
public class Header {

    private String senderId;
    private String receiverId;
    private String correlationId;

    /**
     * Creates a header object.
     * 
     * @param senderId the sender id (HSA ID)
     * @param receiverId the receiver id (HSA ID)
     * 
     * @param correlationId the actual correlation id, a token created in the service bus for each message transaction
     */
    public Header(String senderId, String receiverId, String correlationId) {

        this.senderId = senderId;
        this.receiverId = receiverId;
        this.correlationId = correlationId;
    }

    /**
     * Returns sender id.
     * 
     * @return the sender id
     */
    public String getSenderId() {
        return senderId;
    }
    
    /**
     * Returns receiver id.
     * 
     * @return the receiver id
     */
    public String getReceiverId() {
        return receiverId;
    }
    
    /**
     * Returns correlation id.
     * 
     * @return the correlation id
     */
    public String getCorrelationaid() {
        return correlationId;
    }

}
