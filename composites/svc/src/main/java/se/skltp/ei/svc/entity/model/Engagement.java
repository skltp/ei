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

import se.skltp.ei.svc.entity.model.util.Hash;

/**
 * Entity modeled after the service contract itintegration:engagementindex (r901) <p>
 * 
 * Also see: http://code.google.com/p/rivta/
 */
@Entity(name="engagement_index_table")
public class Engagement {
	
	private static final String NA = "NA";
	private static final String INERA = "Inera";

	// Tech id.
	@Column(name="id", length=36)
	@Id
    private String id = null;

	// Buisness key.
	@Embedded
	private Key key;	
	
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
     * Creates a business key instance.
     * 
     * @return the empty key.
     */
    public static Key createKey() {
    	return new Key();
    }

    /**
     * Returns the business key.
     * 
     * @return the e-index key.
     */
	public Key getKey() {
		return key;
	}
	
	/**
	 * Sets the business key.
	 * 
	 * @param key the key, must exists in order to persist entity.
	 */
	public void setKey(final Key key) {
		this.key = key;
		this.id = key.generateHashKey();
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
	 * Realizes the business key.
	 */
	@Embeddable
	public static class Key implements Serializable {
		//
		private static final long serialVersionUID = 1L;
		// Fields that are part of the business key
		@Column(name="registered_resident_identification")
	    private String registeredResidentIdentification;
		@Column(name="service_domain")
	    private String serviceDomain;
		@Column(name="categorization")
	    private String categorization = NA;
	    @Column(name="logical_address")
	    private String logicalAddress;
	    @Column(name="business_object_instance_identifier")
	    private String businessObjectInstanceIdentifier = NA;
	    @Column(name="source_system")
	    private String sourceSystem;
	    @Column(name="owner")
	    private String owner = INERA;
	    @Column(name="clinical_process_interest_id")    
	    private String clinicalProcessInterestId = NA;
		
	    //
	    private Key() {}
	    
	    @Override
	    public boolean equals(Object r) {
	    	if (r == null) {
	    		return false;
	    	} else if (this == r) {
	    		return true;
	    	} else if (r instanceof Key) {
	    		Key other = (Key)r;
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
	    	return generateHashKey().hashCode();
	    }
	    	   
	    /**
	     * Returns if the strings are equal.
	     * 
	     * @param l the left hand side string.
	     * @param r the other string.
	     * @return true if equal, otherwise false.
	     */
	    private static boolean eq(String l, String r) {
	    	return nvl(l, "").equals(nvl(r, ""));
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
			this.categorization = nvl(categorization, NA);
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
			this.businessObjectInstanceIdentifier = nvl(businessObjectInstanceIdentifier, NA);
		}
		
		public String getSourceSystem() {
			return sourceSystem;
		}
		
		public void setSourceSystem(String sourceSystem) {
			this.sourceSystem = sourceSystem;
		}
		
		public String getOwner() {
			return owner;
		}
		
		public void setOwner(String owner) {
			this.owner = nvl(owner, INERA);
		}
		
		public String getClinicalProcessInterestId() {
			return clinicalProcessInterestId;
		}
		
		public void setClinicalProcessInterestId(String clinicalProcessInterestId) {
			this.clinicalProcessInterestId = nvl(clinicalProcessInterestId, NA);
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
		private String generateHashKey() {
			String hash = Hash.shaHash(registeredResidentIdentification,
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