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
import se.skltp.ei.svc.service.api.EiException;
import se.skltp.ei.svc.service.api.Header;
import se.skltp.ei.svc.service.api.ProcessInterface;

public class ProcessBean implements ProcessInterface {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessBean.class);

	private String owner;

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
    	validateLogicalAddress(header);
        validateUniqueness(request);
    }

    // Update, R1: Validate uniqueness within the request
	private void validateUniqueness(UpdateType request) {
		validateUniqueness(request.getEngagementTransaction());
	}
	
	// R1 - validate uniqueness for engagement in EngangementTransactions
	private void validateUniqueness(List<EngagementTransactionType> engagementTransactions ) {
		final Map<String, Integer> hashCodes = new HashMap<String, Integer>(engagementTransactions.size());
        int hashCodeIndex = 0;

        for (final EngagementTransactionType engagementTransaction : engagementTransactions) {
            final Engagement e = toEntity(engagementTransaction.getEngagement());
            final Integer otherIndex = hashCodes.put(e.getId(), ++hashCodeIndex);
            if (otherIndex != null) {
            	throw new EiException(EI002_DUPLICATE_UPDATE_ENTRIES, otherIndex, hashCodeIndex);
            }
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
		validateUniqueness(request); // Update R1
	}
	
	/**
	 * R1 - Validate uniqueness within the request
	 * @param request
	 * @throws EiException when the validation fail
	 */
	private void validateUniqueness(ProcessNotificationType request) {
		validateUniqueness(request.getEngagementTransaction());
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

			// We should use the owner provided in the engagement

			final Engagement e = toEntity(et);
		
			// If the owner of the engagement is same as the owner of the index, just drop the engagement
			// since there is nothing for us to process here.
			if(e.getOwner() == this.owner) {
				LOG.info("Dropping this notification since this EI already is the owner of the Engagement");
				continue;
			}
			
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

		return NOTIFICATION_RESPONSE_OK;
		
		// TODO (patrik) - hantera ut uppdatering av poster med annan owner ska göras
	}

	
	/**
	 * Removes all engagements that has this index as owner 
	 * @param request 
	 */
	@Override
	public ProcessNotificationType filterProcessNotification(ProcessNotificationType request) {

		Iterator<EngagementTransactionType> iter = request.getEngagementTransaction().iterator();
		while(iter.hasNext()) {
			EngagementTransactionType e = iter.next();
			if(e.getEngagement().getOwner() == this.owner) {
				iter.remove();
			}
		}
		
		return request;
	}
	

}