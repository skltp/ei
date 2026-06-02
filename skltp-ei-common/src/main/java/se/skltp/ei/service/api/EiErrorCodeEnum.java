/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.service.api;

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
