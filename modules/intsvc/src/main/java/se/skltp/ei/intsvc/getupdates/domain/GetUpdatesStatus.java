package se.skltp.ei.intsvc.getupdates.domain;

import java.util.Date;

/**
 * Author: Henrik Rostam
 */

public class GetUpdatesStatus {

    private String logicalAddress;
    private String serviceDomain;
    private Date lastSuccess;
    private int amountOfErrorsSinceLastSuccess;

    public String getLogicalAddress() {
        return logicalAddress;
    }

    public void setLogicalAddress(String logicalAddress) {
        this.logicalAddress = logicalAddress;
    }

    public String getServiceDomain() {
        return serviceDomain;
    }

    public void setServiceDomain(String serviceDomain) {
        this.serviceDomain = serviceDomain;
    }

    public Date getLastSuccess() {
        return lastSuccess;
    }

    public void setLastSuccess(Date lastSuccess) {
        this.lastSuccess = lastSuccess;
    }

    public int getAmountOfErrorsSinceLastSuccess() {
        return amountOfErrorsSinceLastSuccess;
    }

    public void setAmountOfErrorsSinceLastSuccess(int amountOfErrorsSinceLastSuccess) {
        this.amountOfErrorsSinceLastSuccess = amountOfErrorsSinceLastSuccess;
    }

}
