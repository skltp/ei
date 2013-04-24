package se.skltp.ei.svc.service.api;

public enum EiErrorCodeEnum {
    EI000_TECHNICAL_ERROR("EI000", "A technical error has occurred, error message: {0}"),
    EI001_XSD_VALIDATION_ERROR("EI001", "The payload does not follow the XML Schema, error messge: {0}"),
    EI002_DUPLICATE_UPDATE_ENTRIES("EI002", "EngagementTransaction #{0} and #{1} have the same key. That is not allowed. See rule for Update-R1 in service contract"),
    EI003_LOGICALADDRESS_DONT_MATCH_OWNER("EI003","Invalid routing. Logical address is {0} but the owner is {1}. They must be the same. See rule for Update-R7 in service contract");

    private final String code;
    private final String msg;

    private EiErrorCodeEnum(String code, String msg) {
    	this.code = code;
        this.msg = msg;
    }

    public String getErrorCode() {
        return code;
    }	
    public String getMessageFormat() {
        return msg;
    }	
}
