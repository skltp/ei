/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.skltp.ei.svc.entity.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
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
@Entity(name=Engagement.ENGAGEMENT_INDEX_TABLE)
@Table(appliesTo=Engagement.ENGAGEMENT_INDEX_TABLE,
indexes={ @Index(name="engagement_search_index", 
columnNames= { Engagement.REGISTERED_RESIDENT_ID, Engagement.SERVICE_DOMAIN, Engagement.CATEGORIZATION }) })
public class Engagement implements BusinessKey {

    static final String ENGAGEMENT_INDEX_TABLE = "engagement_index_table";
    static final String REGISTERED_RESIDENT_ID = "registered_resident_id";
    static final String SERVICE_DOMAIN = "service_domain";
    static final String CATEGORIZATION = "categorization";
    static final String LOGICAL_ADDRESS = "logical_address";
    static final String BUSINESS_OBJECT_INSTANCE_ID = "business_object_instance_id";
    static final String SOURCE_SYSTEM = "source_system";
    static final String DATA_CONTROLLER = "data_controller";
    static final String OWNER = "owner";
    static final String CLINICAL_PROCESS_INTEREST_ID = "clinical_process_interest_id";
    static final String MOST_RECENT_CONTENT = "most_recent_content";

    static final String NA = "NA";
    static final String INERA = "Inera";

    // Tech id.
    @Column(name="id", length=64)
    @Id
    private String id;

    // complex key (real primary key)

    @Column(name=REGISTERED_RESIDENT_ID, nullable=false, length=32, updatable=false)
    private String registeredResidentIdentification;

    @Column(name=SERVICE_DOMAIN, nullable=false, length=255, updatable=false)
    private String serviceDomain;

    @Column(name=CATEGORIZATION, nullable=false, length=255, updatable=false)
    private String categorization = NA;

    @Column(name=LOGICAL_ADDRESS, nullable=false, length=64, updatable=false)
    private String logicalAddress;

    @Column(name=BUSINESS_OBJECT_INSTANCE_ID, nullable=false, length=128, updatable=false)
    private String businessObjectInstanceIdentifier = NA;

    @Column(name=SOURCE_SYSTEM, nullable=false, length=64, updatable=false)
    private String sourceSystem;

    @Column(name=DATA_CONTROLLER, nullable=false, length=64, updatable=false)
    private String dataController;

    @Column(name=OWNER, nullable=false, length=64, updatable=false)
    private String owner = INERA;

    @Column(name=CLINICAL_PROCESS_INTEREST_ID, nullable=false, length=128, updatable=false)    
    private String clinicalProcessInterestId = NA;

    // Other non business key fields
    @Column(name=MOST_RECENT_CONTENT)
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
        this.registeredResidentIdentification = registeredResidentIdentification;
        this.serviceDomain = serviceDomain;
        this.categorization = nvl(categorization, NA);
        this.logicalAddress = logicalAddress;
        this.businessObjectInstanceIdentifier = nvl(businessObjectInstanceIdentifier, NA);
        this.sourceSystem = sourceSystem;
        this.dataController = dataController;
        this.owner = nvl(owner, INERA);
        this.clinicalProcessInterestId = nvl(clinicalProcessInterestId, NA);
        this.id = generateHashId();
    }

    /**
     * Returns current timestamp.
     * 
     * @return the timestamp.
     */
    private static Date now() {
        return new Date();
    }

    @PrePersist
    void onPrePersist() {
        setCreationTime(now());
    }

    @PreUpdate
    void onPreUpdate() {
        setUpdateTime(now());
    }

    @Override
    public boolean equals(Object r) {
        if (this == r) {
            return true;
        } else if (r == null || this.id == null) {
            return false;
        } else if (r instanceof Engagement) {
            return this.id.equals(((Engagement)r).id);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (id == null) ? 0 : getId().hashCode();
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

    void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;

    }
    public Date getUpdateTime() {
        return updateTime;
    }

    void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
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

    public String getDataController() {
        return dataController;
    }

    //
    public BusinessKey getBusinessKey() {
        return this;
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

}