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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;

import se.skltp.ei.svc.entity.model.util.Hash;

/**
 * Entity modeled after the service contract itintegration:engagementindex (r901) <p>
 * 
 * Uses a SHA-256 generated hash-key in hex string format as an unique id.
 * 
 * Also see: http://code.google.com/p/rivta/
 */
@Entity(name="engagement_index_table")
@Table(appliesTo="engagement_index_table",
indexes={ @Index(name="engagement_search_index", columnNames="registered_resident_identification") })
public class Engagement {

    private static final String NA = "NA";
    private static final String INERA = "Inera";

    // Tech id.
    @Column(name="id", length=64)
    @Id
    private String id;

    // Business key.
    @Embedded
    private BusinessKey businessKey;	

    // Other non business key fields
    @Column(name="most_recent_content")
    @Temporal(TemporalType.TIMESTAMP)
    private Date mostRecentContent;

    // set by consumer
    @Column(name="creation_time", nullable=false, updatable=false)    
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    // set by consumer
    @Column(name="update_time")    
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;


    /**
     * Sets the business key.
     */
    public void setBusinessKey(String registeredResidentIdentification,
            String serviceDomain,
            String categorization,
            String logicalAddress,
            String businessObjectInstanceIdentifier,
            String sourceSystem,
            String dataController,
            String owner,
            String clinicalProcessInterestId) {
        businessKey = new BusinessKey(registeredResidentIdentification,
                serviceDomain,
                categorization,
                logicalAddress,
                businessObjectInstanceIdentifier,
                sourceSystem,
                dataController,
                owner,
                clinicalProcessInterestId);
        id = businessKey.getHashId();
    }

    @Override
    public boolean equals(Object r) {
        if (this == r) {
            return true;
        } else if (r == null) {
            return false;
        } else if (r instanceof Engagement) {
            return getId().equals(((Engagement)r).getId());
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return getId().hashCode();
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
        
        // complex key (real primary key)
        
        @Column(name="registered_resident_identification", nullable=false, length=32, updatable=false)
        private String registeredResidentIdentification;

        @Column(name="service_domain", nullable=false, length=255, updatable=false)
        private String serviceDomain;

        @Column(name="categorization", nullable=false, length=255, updatable=false)
        private String categorization = NA;

        @Column(name="logical_address", nullable=false, length=64, updatable=false)
        private String logicalAddress;

        @Column(name="business_object_instance_identifier", nullable=false, length=128, updatable=false)
        private String businessObjectInstanceIdentifier = NA;
        
        @Column(name="source_system", nullable=false, length=64, updatable=false)
        private String sourceSystem;

        @Column(name="data_controller", nullable=false, length=64, updatable=false)
        private String dataController;

        @Column(name="owner", nullable=false, length=64, updatable=false)
        private String owner = INERA;

        @Column(name="clinical_process_interest_id", nullable=false, length=128, updatable=false)    
        private String clinicalProcessInterestId = NA;

        // single business key hash value (derived) 
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
                String dataController,
                String owner,
                String clinicalProcessInterestId) {
            this.registeredResidentIdentification = registeredResidentIdentification;
            this.serviceDomain = serviceDomain;
            this.categorization = nvl(categorization, NA);
            this.logicalAddress = logicalAddress;
            this.businessObjectInstanceIdentifier = nvl(businessObjectInstanceIdentifier, NA);
            this.sourceSystem = sourceSystem;
            this.dataController = dataController;
            this.owner = nvl(owner, INERA);
            this.clinicalProcessInterestId = nvl(clinicalProcessInterestId, NA);
            this.hashId = null;
        }

        @Override
        public boolean equals(Object r) {
            if (this == r) {
                return true;
            } else if (r == null) {
                return false;
            } else if (r instanceof BusinessKey) {
                return getHashId().equals(((BusinessKey)r).getHashId());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return getHashId().hashCode();
        }

        /**
         * Returns the unique hash id and primary key.
         * 
         * @return the unique hash id.
         */
        public String getHashId() {
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
                    dataController,
                    owner,
                    clinicalProcessInterestId);
            return hash;
        }

        public String getDataController() {
            return dataController;
        }
    }
}