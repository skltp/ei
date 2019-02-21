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
 * Entity modeled after the service contract itintegration:engagementindex (1.0) <p>
 * 
 * Uses a SHA-256 generated hash-key in hex string format as an unique id. <p>
 * 
 * Also see <a href="https://code.google.com/p/rivta/wiki/ServiceDomainTable">RIV-TA Service Contract Page</a> 
 * to find more detailed descriptions.
 * 
 * @author Peter
 */
@Entity(name=Engagement.ENGAGEMENT_INDEX_TABLE)
@Table(appliesTo=Engagement.ENGAGEMENT_INDEX_TABLE,
indexes={ @Index(name=Engagement.ENGAGEMENT_SEARCH_INDEX, 
columnNames= { Engagement.REGISTERED_RESIDENT_ID, Engagement.SERVICE_DOMAIN, Engagement.CATEGORIZATION }) })
public class Engagement implements BusinessKey {

    // database names
    static final String ENGAGEMENT_INDEX_TABLE = "engagement_index_table";
    static final String ENGAGEMENT_SEARCH_INDEX = "engagement_search_index";
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

    // constants
    static final String NA = "NA";

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
    private String owner;

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
     * 
     * @param registeredResidentIdentification the resident id
     * @param serviceDomain the service domain
     * @param categorization the categorization
     * @param logicalAddress the logical address
     * @param businessObjectInstanceIdentifier the business object instance id.
     * @param sourceSystem the source system
     * @param dataController the data controller
     * @param owner the owner (e-index record owner)
     * @param clinicalProcessInterestId the process interest id
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
        this.owner = owner;
        this.clinicalProcessInterestId = nvl(clinicalProcessInterestId, NA);
        this.id = generateHashId();
    }

    /**
     * Returns current timestamp.
     * 
     * @return the current timestamp
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


    /**
     * Returns the unique hash id.
     * 
     * @return the id
     */
    public String getId() {  
        return id;
    }

    /**
     * Sets the most recent content timestamp.
     * 
     * @param mostRecentContent the timestamp
     */
    public void setMostRecentContent(Date mostRecentContent) {
        this.mostRecentContent = mostRecentContent;
    }

    /**
     * Returns most recent content timestamp.
     * 
     * @return the timestamp.
     */
    public Date getMostRecentContent() {
        return mostRecentContent;
    }

    /**
     * Returns the creation time.
     * 
     * @return the creation timestamp
     */
    public Date getCreationTime() {
        return creationTime;
    }

    //
    void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
    
    /**
     * Returns last updated timestamp.
     * 
     * @return the timestamp
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    //
    void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String getRegisteredResidentIdentification() {
        return registeredResidentIdentification;
    }

    @Override
    public String getServiceDomain() {
        return serviceDomain;
    }

    @Override
    public String getCategorization() {
        return categorization;
    }

    @Override
    public String getLogicalAddress() {
        return logicalAddress;
    }

    @Override
    public String getBusinessObjectInstanceIdentifier() {
        return businessObjectInstanceIdentifier;
    }

    @Override
    public String getSourceSystem() {
        return sourceSystem;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public String getClinicalProcessInterestId() {
        return clinicalProcessInterestId;
    }

    @Override
    public String getDataController() {
        return dataController;
    }

    /**
     * Returns the business key
     * 
     * @return the business key
     */
    public BusinessKey getBusinessKey() {
        return this;
    }


    /**
     * Returns a default value for empty input string
     * 
     * @param s the input string
     * @param d the default value.
     * @return the input string, or the default value if the input string is empty.
     */
    private static String nvl(String s, String d) {
        return (s == null || s.length() == 0) ? d : s;
    }

    /**
     * Generates a hash id for this post.
     * 
     * @return the generated hash id as a string.
     */
    private String generateHashId() {
        return Hash.generateHashId(this);
    }

    @Override
    public String toString() {
        return "Engagement{" +
                "id='" + id + '\'' +
                ", serviceDomain='" + serviceDomain + '\'' +
                ", categorization='" + categorization + '\'' +
                ", logicalAddress='" + logicalAddress + '\'' +
                ", businessObjectInstanceIdentifier='" + businessObjectInstanceIdentifier + '\'' +
                ", sourceSystem='" + sourceSystem + '\'' +
                ", dataController='" + dataController + '\'' +
                ", owner='" + owner + '\'' +
                ", clinicalProcessInterestId='" + clinicalProcessInterestId + '\'' +
                ", mostRecentContent=" + mostRecentContent +
                ", creationTime=" + creationTime +
                ", updateTime=" + updateTime +
                '}';
    }
}