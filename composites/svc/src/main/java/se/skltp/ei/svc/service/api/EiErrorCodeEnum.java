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
 * Defines all service level application errors.
 * 
 * @author Magnus Larsson
 */
public enum EiErrorCodeEnum {
    EI000_TECHNICAL_ERROR("EI000", "A technical error has occurred, error message: {0}"),
    EI001_XSD_VALIDATION_ERROR("EI001", "The payload does not follow the XML Schema, error messge: {0}"),
    EI002_DUPLICATE_UPDATE_ENTRIES("EI002", "EngagementTransaction #{0} and #{1} have the same key. That is not allowed. See rule for Update-R1 in service contract"),
    EI003_LOGICALADDRESS_DONT_MATCH_OWNER("EI003","Invalid routing. Logical address is {0} but the owner is {1}. They must be the same. See rule for Update-R7 in service contract"),
    EI004_VALIDATION_ERROR("EI004", "The payload does not validate, error messge: {0}"),
    EI005_VALIDATION_ERROR_INVALID_LOGICAL_ADDRESS("EI005", "The logicalAddress in EngagementTransaction #{0} is reserved and not allowed, hsa-id: {1}"),
    EI006_VALIDATION_ERROR_INVALID_SOURCE_SYSTEM("EI006", "The sourceSystem in EngagementTransaction #{0} is reserved and not allowed, hsa-id: {1}");
    
    private final String code;
    private final String msg;

    private EiErrorCodeEnum(String code, String msg) {
    	this.code = code;
        this.msg = msg;
    }

    /**
     * Returns the error code.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return code;
    }	
    
    /**
     * Returns the message format string.
     * 
     * @return the message format string
     */
    public String getMessageFormat() {
        return msg;
    }
    
    /**
     * Creates a corresponding exception.
     * 
     * @param args the arguments to message format string.
     * @return a corresponding exception
     */
    public EiException createException(Object... args) {
        return new EiException(this, args);
    }
}
