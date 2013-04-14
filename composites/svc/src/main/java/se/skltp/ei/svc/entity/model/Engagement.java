/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.skltp.ei.svc.entity.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;

import se.skltp.ei.svc.entity.model.util.Hash;

/**
 * Entity modeled after the service contract itintegration:engagementindex (r901) <p>
 * 
 * Uses a SHA-256 generated hash-key in UUID (3) format as an unique id.
 * 
 * Also see: http://code.google.com/p/rivta/
 */
@Entity(name="engagement_index_table")
@Table(appliesTo="engagement_index_table",
	indexes={ @Index(name="engagement_search_index", columnNames="registered_resident_identification") })
public class Engagement {
	
	private static final String EMPTY = "";
	private static final String NA = "NA";
	private static final String INERA = "Inera";

	// Tech id.
	@Column(name="id", length=64)
	@Id
    private String id = null;

	// Buisness key.
	@Embedded
	private BusinessKey businessKey;	
	
    // Other non business key fields
	@Column(name="most_recent_content")
    @Temporal(TemporalType.TIMESTAMP)
	private Date mostRecentContent;
    @Column(name="creation_time")    
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;
    @Column(name="update_time")    
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    
    @PrePersist
    void onPrePersist() {
    	Date now = new Date();
    	setUpdateTime(now);
    }
    
    /**
     * Sets the business key.
     */
    public void setBusinessKey(String registeredResidentIdentification,
			String serviceDomain,
			String categorization,
			String logicalAddress,
			String businessObjectInstanceIdentifier,
			String sourceSystem,
			String owner,
			String clinicalProcessInterestId) {
    	businessKey = new BusinessKey(registeredResidentIdentification,
    			serviceDomain,
    			categorization,
    			logicalAddress,
    			businessObjectInstanceIdentifier,
    			sourceSystem,
    			owner,
    			clinicalProcessInterestId);
    	id = businessKey.getHashId();
    }

    /**
     * Returns the business key.
     * 
     * @return the e-index key.
     */
	public BusinessKey getBusinessKey() {
		return businessKey;
	}
		
	public String getId() {
		return id;
	}
	
	public void setMostRecentContent(Date mostRecentContent) {
		this.mostRecentContent = mostRecentContent;
	}
	
	public Date getMostRecentContent() {
		return mostRecentContent;
	}
	
	public Date getCreationTime() {
		return creationTime;
	}
	
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
	
	public Date getUpdateTime() {
		return updateTime;
	}
	
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	
	/**
	 * Implements the business key.
	 */
	@Embeddable
	public static class BusinessKey implements Serializable {
		//
		private static final long serialVersionUID = 1L;
		// Fields that are part of the business key
		@Column(name="registered_resident_identification", nullable=false, length=32)
	    private String registeredResidentIdentification;
		@Column(name="service_domain", nullable=false, length=256)
	    private String serviceDomain;
		@Column(name="categorization", nullable=false, length=256)
	    private String categorization = NA;
	    @Column(name="logical_address", nullable=false, length=64)
	    private String logicalAddress;
	    @Column(name="business_object_instance_identifier", nullable=false, length=128)
	    private String businessObjectInstanceIdentifier = NA;
	    @Column(name="source_system", nullable=false, length=64)
	    private String sourceSystem;
	    @Column(name="owner", nullable=false, length=64)
	    private String owner = INERA;
	    @Column(name="clinical_process_interest_id", nullable=false, length=128)    
	    private String clinicalProcessInterestId = NA;
	    //
	    private transient String hashId;
		
	    //
	    protected BusinessKey() {}
	    
	    //
	    protected BusinessKey(String registeredResidentIdentification,
				String serviceDomain,
				String categorization,
				String logicalAddress,
				String businessObjectInstanceIdentifier,
				String sourceSystem,
				String owner,
				String clinicalProcessInterestId) {
	    	this.registeredResidentIdentification = registeredResidentIdentification;
	    	this.serviceDomain = serviceDomain;
	    	this.categorization = nvl(categorization, NA);
	    	this.logicalAddress = logicalAddress;
	    	this.businessObjectInstanceIdentifier = nvl(businessObjectInstanceIdentifier, NA);
	    	this.sourceSystem = sourceSystem;
	    	this.owner = nvl(owner, INERA);
	    	this.clinicalProcessInterestId = nvl(clinicalProcessInterestId, NA);
	    	this.hashId = null;
	    }
	    
	    @Override
	    public boolean equals(Object r) {
	    	if (r == null) {
	    		return false;
	    	} else if (this == r) {
	    		return true;
	    	} else if (r instanceof BusinessKey) {
	    		BusinessKey other = (BusinessKey)r;
	    		return eq(registeredResidentIdentification, other.registeredResidentIdentification)
	    				&& eq(serviceDomain, other.serviceDomain)
	    				&& eq(categorization, other.categorization)
	    				&& eq(logicalAddress, other.logicalAddress)
	    				&& eq(businessObjectInstanceIdentifier, other.businessObjectInstanceIdentifier)
	    				&& eq(sourceSystem, other.sourceSystem)
	    				&& eq(owner, other.owner)
	    				&& eq(clinicalProcessInterestId, other.clinicalProcessInterestId);
	    	}
	    	return false;
	    }
	    
	    @Override
	    public int hashCode() {
	    	return getHashId().hashCode();
	    }
	    	   
	    /**
	     * Returns if the strings are equal.
	     * 
	     * @param l the left hand side string.
	     * @param r the other string.
	     * @return true if equal, otherwise false.
	     */
	    private static boolean eq(String l, String r) {
	    	return nvl(l, EMPTY).equals(nvl(r, EMPTY));
	    }
	    
	    //
	    protected String getHashId() {
	    	if (hashId == null) {
	    		hashId = generateHashId();
	    	}
	    	return hashId;
	    }
	    
	    public String getRegisteredResidentIdentification() {
			return registeredResidentIdentification;
		}
		
		public String getServiceDomain() {
			return serviceDomain;
		}
				
		public String getCategorization() {
			return categorization;
		}
		
		
		public String getLogicalAddress() {
			return logicalAddress;
		}
		
		public String getBusinessObjectInstanceIdentifier() {
			return businessObjectInstanceIdentifier;
		}
			
		public String getSourceSystem() {
			return sourceSystem;
		}
		
		public String getOwner() {
			return owner;
		}
		
		public String getClinicalProcessInterestId() {
			return clinicalProcessInterestId;
		}
		
		/**
		 * Returns NA constant for empty strings.
		 * 
		 * @param s the input.
		 * @param d the default value.
		 * @return NA if s is empty, otherwise s.
		 */
		private static String nvl(String s, String d) {
			return (s == null || s.length() == 0) ? d : s;
		}
		
		/**
		 * Generates a hash key for this post.
		 */
		private String generateHashId() {
			String hash = Hash.sha2(registeredResidentIdentification,
					serviceDomain,
					categorization,
					logicalAddress,
					businessObjectInstanceIdentifier,
					sourceSystem,
					owner,
					clinicalProcessInterestId);
			return hash;
		}

	}
}