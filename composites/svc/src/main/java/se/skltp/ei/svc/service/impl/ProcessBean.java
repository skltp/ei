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

    //
    private static UpdateResponseType RESPONSE_OK = new UpdateResponseType() {
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
		final Map<String, Integer> hashCodes = new HashMap<String, Integer>(request.getEngagementTransaction().size());
        int hashCodeIndex = 0;

        for (final EngagementTransactionType engagementTransaction : request.getEngagementTransaction()) {
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

	@Override
	public void validateProcessNotification(Header header, ProcessNotificationType parameters) {
	    // TODO. Patrik. Add validation rules here, specifically regarding that the owner field is prsent.
		
	}

	@Override
	public ProcessNotificationResponseType processNotification(Header header, ProcessNotificationType parameters) {
	    // TODO. Patrik. Add business rules here.
		return null;
	}
}