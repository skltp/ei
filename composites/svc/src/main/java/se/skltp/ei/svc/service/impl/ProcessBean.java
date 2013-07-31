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

import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI002_DUPLICATE_UPDATE_ENTRIES;
import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI003_LOGICALADDRESS_DONT_MATCH_OWNER;
import static se.skltp.ei.svc.service.impl.util.EntityTransformer.toEntity;

import java.util.ArrayList;
import java.util.Collections;
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
import se.skltp.ei.svc.service.api.EiErrorCodeEnum;
import se.skltp.ei.svc.service.api.EiException;
import se.skltp.ei.svc.service.api.Header;
import se.skltp.ei.svc.service.api.ProcessInterface;

public class ProcessBean implements ProcessInterface {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessBean.class);

    private String owner;

    // Maximum number of engagements that should be processed
    public static int MAX_NUMBER_OF_ENGAGEMENTS = 1000;


    private EngagementRepository engagementRepository;

    private static UpdateResponseType RESPONSE_OK = new UpdateResponseType() {
        @Override
        public ResultCodeEnum getResultCode() {
            return ResultCodeEnum.OK;
        }
    };

    private static ProcessNotificationResponseType NOTIFICATION_RESPONSE_OK = new ProcessNotificationResponseType() {
        @Override
        public ResultCodeEnum getResultCode() {
            return ResultCodeEnum.OK;
        }
    };


    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Autowired
    public void setEngagementRepository(EngagementRepository engagementRepository) {
        LOG.info("ProcessBean got its engagementRepository injected");
        this.engagementRepository = engagementRepository;
    }

    /**
     * Validates an update request without touching the database.
     * 
     * @param header the header
     * @param request the request
     */
    @Override 
    public void validateUpdate(Header header, UpdateType request) {
        // R7
        validateLogicalAddress(header);

        validateEngagementTransactions(request.getEngagementTransaction(), false);
    }

    //
    private void validateEngagementTransactions(final List<EngagementTransactionType> engagementTransactions, boolean ownerCheck) {
        validateMaxLength(engagementTransactions);

        final Map<String, Integer> hashCodes = new HashMap<String, Integer>(engagementTransactions.size());
        int hashCodeIndex = 0;
        for (final EngagementTransactionType engagementTransaction : engagementTransactions) {
            final EngagementType et = engagementTransaction.getEngagement();

            // R1 uniqueness
            final Engagement e = toEntity(et);
            final Integer otherIndex = hashCodes.put(e.getId(), ++hashCodeIndex);
            if (otherIndex != null) {
                throw new EiException(EI002_DUPLICATE_UPDATE_ENTRIES, otherIndex, hashCodeIndex);
            }

            // mandatory fields
            validateMandatoryFields(et, ownerCheck);

        }

    }

    /**
     * Checks that a mandatory value exists.
     * 
     * @param name the field name.
     * @param value the field value.
     */
    private static void mandatoryValueCheck(String name, String value) {
        if (value == null || value.length() == 0) {
            throw new EiException(EiErrorCodeEnum.EI004_VALIDATION_ERROR, "mandatory field \"" + name + "\" is missing");            
        }
    }

    /**
     * Validates all mandatory fields.
     * 
     * @param et the engagement record to validate.
     * @param ownerCheck true if an owner check shall be performed as well, otherwise false.
     */
    private static void validateMandatoryFields(final EngagementType et, boolean ownerCheck) {
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


    // Update, R7: Logical address in request equals owner of EI
    private void validateLogicalAddress(Header header) {
        if (header == null || header.getReceiverId() == null) {
            throw new EiException(EI003_LOGICALADDRESS_DONT_MATCH_OWNER,
                    "missing", owner);
        }

        if (!header.getReceiverId().equals(owner)) {
            throw new EiException(EI003_LOGICALADDRESS_DONT_MATCH_OWNER,
                    header.getReceiverId(), owner);
        }
    }

    // Update/processNotification - max 1000 engagements per request
    private static void validateMaxLength(List<EngagementTransactionType> engagementTransactions ) {

        if(engagementTransactions.size() > MAX_NUMBER_OF_ENGAGEMENTS) {
            throw new EiException(EiErrorCodeEnum.EI000_TECHNICAL_ERROR, "The request contains more than " + 
                    MAX_NUMBER_OF_ENGAGEMENTS + " engagements. Maximum number of engagements per request is " + MAX_NUMBER_OF_ENGAGEMENTS + ".");
        }
    }


    /**
     * Performs an index update transaction. <p>
     * 
     * Due to the fact that no underlying XA resources is in use, this transaction will 
     * be completely standalone and not to be confused with other transactions, i.e. already 
     * started JMS transactions in modules/insvc.
     * 
     * @param header the header
     * @param request the request
     * @return the update response
     */
    @Override
    @Transactional(isolation=Isolation.READ_UNCOMMITTED)
    public UpdateResponseType update(Header header, UpdateType request) {
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

        // Perform the delete if any
        if (deleteList != null) {
            engagementRepository.delete(deleteList);
        }

        // Perform the save
        engagementRepository.save(saveList);  	

        return RESPONSE_OK;
    }

    /**
     * Validates a processNotifcation request without touching the database.
     * 
     * @param header the header
     * @param request the request
     */
    @Override
    public void validateProcessNotification(Header header, ProcessNotificationType request) {
        validateEngagementTransactions(request.getEngagementTransaction(), true);
    }


    /**
     * Performs an index update of based on a ProcessNotification
     * 
     * @param header the header
     * @param request the request
     * @return the processNotifcation response
     */
    @Override
    @Transactional(isolation=Isolation.READ_UNCOMMITTED)
    public ProcessNotificationResponseType processNotification(Header header, ProcessNotificationType request) {
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

        return NOTIFICATION_RESPONSE_OK;
    }


    /**
     * Removes all engagements that has this index as owner 
     * @param request 
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


}