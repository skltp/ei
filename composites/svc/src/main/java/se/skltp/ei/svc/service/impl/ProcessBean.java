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
package se.skltp.ei.svc.service.impl;

import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI000_TECHNICAL_ERROR;
import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI002_DUPLICATE_UPDATE_ENTRIES;
import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI003_LOGICALADDRESS_DONT_MATCH_OWNER;
import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI004_VALIDATION_ERROR;
import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI005_VALIDATION_ERROR_INVALID_LOGICAL_ADDRESS;
import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI006_VALIDATION_ERROR_INVALID_SOURCE_SYSTEM;
import static se.skltp.ei.svc.service.impl.util.EntityTransformer.toEntity;
import static se.skltp.ei.svc.service.impl.util.EntityTransformer.formatDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.api.Header;
import se.skltp.ei.svc.service.api.ProcessInterface;

/**
 * Updates engagement index with either update or process notification requests.
 * 
 * @author Magnus Larsson
 */
public class ProcessBean implements ProcessInterface {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessBean.class);
    
    private String owner;
    private String pseudonym;

    private EngagementRepository engagementRepository;
	private List<String> updateNotificationNotAllowedHsaIdList;


    @SuppressWarnings("unused")
	private static UpdateResponseType RESPONSE_OK = new UpdateResponseType() {
        @Override
        public ResultCodeEnum getResultCode() {
            return ResultCodeEnum.OK;
        }
    };

    @SuppressWarnings("unused")
	private static ProcessNotificationResponseType NOTIFICATION_RESPONSE_OK = new ProcessNotificationResponseType() {
        @Override
        public ResultCodeEnum getResultCode() {
            return ResultCodeEnum.OK;
        }
    };


    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setPseudonym(String pseudonym) {
        this.pseudonym = pseudonym;
    }

    /**
     * Sets/injects the database repository.
     * 
     * @param engagementRepository the actual repository to use
     */
    @Autowired
    public void setEngagementRepository(EngagementRepository engagementRepository) {
        LOG.info("ProcessBean got its engagementRepository injected");
        this.engagementRepository = engagementRepository;
    }
    
    public void setUpdateNotificationNotAllowedHsaIdList(String setUpdateNotificationNotAllowedHsaIdListString) {
    	updateNotificationNotAllowedHsaIdList = Arrays.asList(setUpdateNotificationNotAllowedHsaIdListString.split(","));
    }


    /**
     * {@inheritDoc}
     */
    @Override 
    public void validateUpdate(Header header, UpdateType request) {
        // R7
        validateLogicalAddress(header);

        validateEngagementTransactions(request.getEngagementTransaction(), false);
    }

    /**
     * Validates all aspects of the list of engagement transactions.
     * 
     * @param engagementTransactions the list.
     * @param ownerCheck true if mandatory owner check shall be carried out as well, otherwise false.
     */
    private void validateEngagementTransactions(final List<EngagementTransactionType> engagementTransactions, boolean ownerCheck) {
        validateMinLength(engagementTransactions);
    	
    	validateMaxLength(engagementTransactions);

        final Map<String, Integer> hashCodes = new HashMap<String, Integer>(engagementTransactions.size());
        int hashCodeIndex = 0;
        for (final EngagementTransactionType engagementTransaction : engagementTransactions) {
            final EngagementType et = engagementTransaction.getEngagement();

            // R1 uniqueness
            final Engagement e = toEntity(et);
            final Integer otherIndex = hashCodes.put(e.getId(), ++hashCodeIndex);
            if (otherIndex != null) {
                throw EI002_DUPLICATE_UPDATE_ENTRIES.createException(otherIndex, hashCodeIndex);
            }

            // Validate that mandatory fields do not start/end with white space
            validateWhiteSpace(et);

            // Validate that reserved hsa-id's (the platforms own hsa-id's for example) are not used by mistake
            // If used it could cause a aggregating servie to call itself with no end, a looping service...
            validateTransactionLogicalAdressAndSourceSystem(hashCodeIndex, et);
            
            // mandatory fields
            validateMandatoryFields(et, ownerCheck);

        }
    }

	private void validateTransactionLogicalAdressAndSourceSystem(int etIndex, EngagementType et) {
    	
    	// If no black-list is set then simply bail out without any validations
    	if (updateNotificationNotAllowedHsaIdList == null) return;
    	
    	if (updateNotificationNotAllowedHsaIdList.contains(et.getLogicalAddress())) {
    		throw EI005_VALIDATION_ERROR_INVALID_LOGICAL_ADDRESS.createException(etIndex, et.getLogicalAddress());
    	}
    	if (updateNotificationNotAllowedHsaIdList.contains(et.getSourceSystem())) {
    		throw EI006_VALIDATION_ERROR_INVALID_SOURCE_SYSTEM.createException(etIndex, et.getSourceSystem());
    	}
	}

	/**
     * Checks that a mandatory value exists.
     * 
     * @param name the field name.
     * @param value the field value.
     */
    private void mandatoryValueCheck(String name, String value) {
        if (value == null || value.length() == 0) {
            throw EI004_VALIDATION_ERROR.createException("mandatory field \"" + name + "\" is missing");            
        }
    }

	/**
     * Checks that value does not have a white space in begining or end.
     * 
     * @param name the field name.
     * @param value the field value.
     */
    private void whitespaceValueCheck(String name, String value) {
    	
    	// Validated elsewhere
        if (value == null || value.length() == 0)
        	return;

        // Check that trimmed string has the same length as original
        if(value.trim().length() != value.length()) {
            throw EI004_VALIDATION_ERROR.createException("mandatory field \"" + name + "\" contains white space in beginning or end");                    	
        }
    }

    /**
     * Validates all mandatory fields.
     * 
     * @param et the engagement record to validate.
     * @param ownerCheck true if an owner check shall be performed as well, otherwise false.
     */
    private void validateMandatoryFields(final EngagementType et, boolean ownerCheck) {
        mandatoryValueCheck("registeredResidentIdentification", et.getRegisteredResidentIdentification());          
        mandatoryValueCheck("serviceDomain", et.getServiceDomain());
        mandatoryValueCheck("categorization", et.getCategorization());
        mandatoryValueCheck("logicalAddress", et.getLogicalAddress());
        mandatoryValueCheck("businessObjectInstanceIdentifier", et.getBusinessObjectInstanceIdentifier());
        mandatoryValueCheck("clinicalProcessInterestId", et.getClinicalProcessInterestId());
        mandatoryValueCheck("sourceSystem", et.getSourceSystem());
        mandatoryValueCheck("dataController", et.getDataController()); 
        // owner
        if (ownerCheck) {
            mandatoryValueCheck("owner", et.getOwner());
        }
    }

    private void validateWhiteSpace(final EngagementType et) {
    	whitespaceValueCheck("registeredResidentIdentification", et.getRegisteredResidentIdentification());          
    	whitespaceValueCheck("serviceDomain", et.getServiceDomain());
    	whitespaceValueCheck("categorization", et.getCategorization());
    	whitespaceValueCheck("logicalAddress", et.getLogicalAddress());
    	whitespaceValueCheck("businessObjectInstanceIdentifier", et.getBusinessObjectInstanceIdentifier());
    	whitespaceValueCheck("clinicalProcessInterestId", et.getClinicalProcessInterestId());
    	whitespaceValueCheck("sourceSystem", et.getSourceSystem());
    	whitespaceValueCheck("dataController", et.getDataController()); 
    }

    // Update, R7: Logical address in request equals owner of EI
    private void validateLogicalAddress(Header header) {
        if (header == null || header.getReceiverId() == null || header.getReceiverId().length() == 0) {
            throw EI003_LOGICALADDRESS_DONT_MATCH_OWNER.createException("missing", owner);
        }

        if (!header.getReceiverId().equals(owner) && !header.getReceiverId().equals(pseudonym) ) {
            throw EI003_LOGICALADDRESS_DONT_MATCH_OWNER.createException(header.getReceiverId(), owner);
        }
    }


    // Update/processNotification - max 1000 engagements per request
    private void validateMaxLength(List<EngagementTransactionType> engagementTransactions ) {
        if(engagementTransactions.size() > MAX_NUMBER_OF_ENGAGEMENTS) {
            throw EI000_TECHNICAL_ERROR.createException("The request contains more than " + 
                    MAX_NUMBER_OF_ENGAGEMENTS + " engagements. Maximum number of engagements per request is " + MAX_NUMBER_OF_ENGAGEMENTS + ".");
        }
    }

    // Update/processNotification - min 1 engagement per request
    private void validateMinLength(List<EngagementTransactionType> engagementTransactions) {
		if (engagementTransactions.size() < MIN_NUMBER_OF_ENGAGEMENTS) {
            throw EI000_TECHNICAL_ERROR.createException("The request contains less than " + 
                    MIN_NUMBER_OF_ENGAGEMENTS + " engagements. Minium number of engagements per request is " + MIN_NUMBER_OF_ENGAGEMENTS + ".");			
		}
	}


    /**
     * {@inheritDoc}
     * 
     * <p>
     * 
     * Due to the fact that no underlying XA resources is in use, this transaction will 
     * be completely standalone and not to be confused with other transactions, i.e. already 
     * started JMS transactions in modules/insvc.
     */
    @Override
    @Transactional(isolation=Isolation.READ_UNCOMMITTED)
    public List<EngagementTransactionType> update(Header header, UpdateType request) {
        LOG.debug("The svc.update service is called");

        // Separate deletes from the saves...
        final List<EngagementTransactionType> engagementTransactions = request.getEngagementTransaction();
        final List<Engagement> saveList = new ArrayList<Engagement>(engagementTransactions.size());
        List<Engagement> deleteList = null;
        for (final EngagementTransactionType engagementTransaction : engagementTransactions) {

            EngagementType et = engagementTransaction.getEngagement();
            et.setOwner(this.owner); // According to R6 owner should always be set to owner of the index

            final Engagement e = toEntity(et);   

            if (engagementTransaction.isDeleteFlag()) {
                if (deleteList == null) {
                    deleteList = new ArrayList<Engagement>();
                }
                deleteList.add(e);
            } else {
                saveList.add(e);
            }
        }

        // Create a list of EngagementTransactions that we will use as a base for Notifications
        final List<EngagementTransactionType> notificationTransactions = new ArrayList<EngagementTransactionType>();
        
        // Get a hashmap with data for most_recent_content
        Map<String, Engagement>existingContent = getEngagementsThatExistsWithContent(request);
        
        // Loop over our incoming list of data and move data to new list if a notification should be sent
        for (final EngagementTransactionType inEngagementTransaction : engagementTransactions) {

            EngagementType inET = inEngagementTransaction.getEngagement();
            inET.setOwner(this.owner); // According to R6 owner should always be set to owner of the index
            
        	Engagement inEngagement = toEntity(inET); 
        	
            boolean keyExists = existingContent.containsKey(inEngagement.getId());
        	Engagement curEngagement  = keyExists ? existingContent.get(inEngagement.getId()) : null; 
        	boolean valueExists = keyExists && curEngagement != null;
            
            // We must set a creationTime and updateTime for notifications.
            Date creationTime = valueExists ? curEngagement.getCreationTime() : null;
        	if(creationTime != null)
        		inET.setUpdateTime(formatDate(new Date()));
            inET.setCreationTime(formatDate(creationTime == null ? new Date() : creationTime));
        		   
            // If delete flag is set add it to notificationList
            if (inEngagementTransaction.isDeleteFlag()) {
            	notificationTransactions.add(inEngagementTransaction);
            } else {
            	// Check if we find this record in our map
            	if (keyExists) {
            		// Check if most_recent_content has changed, it could be NULL
            		String inValue = inEngagement.getMostRecentContent() == null ? "null":formatDate(inEngagement.getMostRecentContent());
            		String dbValue = (!valueExists || curEngagement.getMostRecentContent() == null) ? "null":formatDate(curEngagement.getMostRecentContent());
            		
            		if (inValue.equalsIgnoreCase(dbValue) ) {
            			// Don't add to processNotification list!
            			continue;
            		} else {
                		notificationTransactions.add(inEngagementTransaction);
            		}
            	} else {
            		notificationTransactions.add(inEngagementTransaction);
            	}
            }
        }
                
        // Perform the delete if any
        if (deleteList != null) {
            engagementRepository.delete(deleteList);
        }

        // Perform the save
        engagementRepository.save(saveList);  
        
        // Return a list of EngagementTransactions for notification to subscribers
        return notificationTransactions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateProcessNotification(Header header, ProcessNotificationType request) {
        validateEngagementTransactions(request.getEngagementTransaction(), true);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(isolation=Isolation.READ_UNCOMMITTED)
    public List<EngagementTransactionType> processNotification(Header header, ProcessNotificationType request) {
        LOG.debug("The svc.processNotification service is called");

        // Separate deletes from the saves...
        final List<EngagementTransactionType> engagementTransactions = request.getEngagementTransaction();
        final List<Engagement> saveList = new ArrayList<Engagement>(engagementTransactions.size());
        List<Engagement> deleteList = null;
        for (final EngagementTransactionType engagementTransaction : engagementTransactions) {

            EngagementType et = engagementTransaction.getEngagement();

            final Engagement e = toEntity(et);	
            if (engagementTransaction.isDeleteFlag()) {
                if (deleteList == null) {
                    deleteList = new ArrayList<Engagement>();
                }
                deleteList.add(e);
            } else {
                saveList.add(e);
            }
        }


        // R5 - fetch all posts that should be removed since the incoming Engagement changed owner
        List<Engagement> engagementsWithNewOwners = getEngagementsWithNewOwners(request);
        if(engagementsWithNewOwners.size() > 0) {
            if(deleteList == null) {
                deleteList = new ArrayList<Engagement>();
            }

            for(final Engagement e : engagementsWithNewOwners) {
                LOG.warn("The owner has changed for Engagement with ID: " +  e.getId());
                deleteList.add(e);
            }
        }

        // Perform the delete if any
        if (deleteList != null) {
            engagementRepository.delete(deleteList);
        }

        // Perform the save
        engagementRepository.save(saveList);
        
        // Return a list of EngagementTransactions for now, we will remove duplicate add/updates later
        return engagementTransactions;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessNotificationType filterProcessNotification(ProcessNotificationType request) {

        final Iterator<EngagementTransactionType> iter = request.getEngagementTransaction().iterator();
        while(iter.hasNext()) {
            EngagementTransactionType e = iter.next();
            if(e.getEngagement().getOwner().equals(this.owner)) {
                iter.remove();
            }
        }

        return request;
    }


    /**
     * R5 - fetch all posts that should be removed since the incoming Engagement changed owner
     * 
     * This method is not part of the public API
     * TODO (patrik) - refactor the test so the this method can be private
     * 
     * @param request
     * @return List with engagements to remove
     */
    public List<Engagement> getEngagementsWithNewOwners(ProcessNotificationType request) {

        final List<EngagementTransactionType> engagementTransactions = request.getEngagementTransaction();
        final List<String> ids = new ArrayList<String>(engagementTransactions.size());

        for (final EngagementTransactionType engagementTransaction : engagementTransactions) {
            EngagementType et = engagementTransaction.getEngagement();
            ids.add(toEntity(et, this.owner).getId());
        }

        // The request fail if findByIdIn receives an empty list
        if (ids.size() == 0) {
            return Collections.emptyList();
        } else {
            return engagementRepository.findByIdIn(ids);
        }
    }

    public Map<String, Engagement> getEngagementsThatExistsWithContent(UpdateType request) {

        final List<EngagementTransactionType> engagementTransactions = request.getEngagementTransaction();
        final List<String> ids = new ArrayList<String>(engagementTransactions.size());

        for (final EngagementTransactionType engagementTransaction : engagementTransactions) {
            EngagementType et = engagementTransaction.getEngagement();
            ids.add(toEntity(et, this.owner).getId());
        }

        // The request fail if findByIdIn receives an empty list
        if (ids.size() == 0) {
            return Collections.emptyMap();
        } else {
        	// Create a new HashMap with id as key and most_recent_time as value
        	HashMap<String, Engagement> returnMap = new HashMap<String,Engagement>();
        	for (Engagement engagement :engagementRepository.findByIdIn(ids) ) {
        		returnMap.put(engagement.getId(), engagement);
        	}
            return returnMap;
        }
    }

}