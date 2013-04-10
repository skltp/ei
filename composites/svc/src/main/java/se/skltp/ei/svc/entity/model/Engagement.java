/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.skltp.ei.svc.entity.model;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Entity modeled after the service contract itintegration:engagementindex (r901) found at http://code.google.com/p/rivta/
 */
@Entity
public class Engagement {

    @Id
    @GeneratedValue
    private Long id;

    private String registeredResidentIdentification;
    private String serviceDomain;
    private String categorization;
    private String logicalAddress;
    private String businessObjectInstanceIdentifier;
    private String sourceSystem;
    private Timestamp creationTime;
    private String owner;
    private Timestamp updateTime;
    private String clinicalProcessInterestId;

    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getRegisteredResidentIdentification() {
		return registeredResidentIdentification;
	}
	public void setRegisteredResidentIdentification(
			String registeredResidentIdentification) {
		this.registeredResidentIdentification = registeredResidentIdentification;
	}
	public String getServiceDomain() {
		return serviceDomain;
	}
	public void setServiceDomain(String serviceDomain) {
		this.serviceDomain = serviceDomain;
	}
	public String getCategorization() {
		return categorization;
	}
	public void setCategorization(String categorization) {
		this.categorization = categorization;
	}
	public String getLogicalAddress() {
		return logicalAddress;
	}
	public void setLogicalAddress(String logicalAddress) {
		this.logicalAddress = logicalAddress;
	}
	public String getBusinessObjectInstanceIdentifier() {
		return businessObjectInstanceIdentifier;
	}
	public void setBusinessObjectInstanceIdentifier(
			String businessObjectInstanceIdentifier) {
		this.businessObjectInstanceIdentifier = businessObjectInstanceIdentifier;
	}
	public String getSourceSystem() {
		return sourceSystem;
	}
	public void setSourceSystem(String sourceSystem) {
		this.sourceSystem = sourceSystem;
	}
	public Timestamp getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(Timestamp creationTime) {
		this.creationTime = creationTime;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public Timestamp getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	public String getClinicalProcessInterestId() {
		return clinicalProcessInterestId;
	}
	public void setClinicalProcessInterestId(String clinicalProcessInterestId) {
		this.clinicalProcessInterestId = clinicalProcessInterestId;
	}
}