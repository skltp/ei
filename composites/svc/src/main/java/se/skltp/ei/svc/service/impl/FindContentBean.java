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

import static se.skltp.ei.svc.service.impl.util.EntityTransformer.fromEntity;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.transaction.annotation.Transactional;

import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import static se.skltp.ei.svc.entity.model.EngagementSpecifications.*;
import se.skltp.ei.svc.service.api.EiErrorCodeEnum;
import se.skltp.ei.svc.service.api.EiException;
import se.skltp.ei.svc.service.api.FindContentInterface;
import se.skltp.ei.svc.service.api.Header;
import se.skltp.ei.svc.service.impl.util.EntityTransformer;

public class FindContentBean implements FindContentInterface {

    private static final Logger LOG = LoggerFactory.getLogger(FindContentBean.class);

    private EngagementRepository engagementRepository;
    
    // Exception messages for missing fields
    public static final String MISSING_PERSON_MESSAGE = "registeredResidentIdentification is mandatory but missing";
    public static final String MISSING_SERVICEDOMAIN_MESSAGE = "serviceDomain is mandatory but missing";
    
    
    @Autowired
    public void setEngagementRepository(EngagementRepository engagementRepository) {
        LOG.info("FindContentBean got its engagementRepository injected: " + engagementRepository);
        this.engagementRepository = engagementRepository;
    }

    /**
     *
     * @param header
     * @param parameters
     * @return
     */
    @Override
    @Transactional(readOnly=true)
    public FindContentResponseType findContent(Header header, FindContentType parameters) {
        LOG.debug("The svc.findContent service is called");
        
        // Validate input
        this.validateFindContent(header, parameters);
        
        FindContentResponseType response = new FindContentResponseType();

        Iterable<Engagement> dbSearchResult = engagementRepository.findAll(createQuery(parameters));

        List<EngagementType> EngagementList = response.getEngagement();
        for (Engagement engagement : dbSearchResult) {
            EngagementList.add(fromEntity(engagement));
        }
        return response;
    }

    /**
     * Creates the standard query.
     * 
     * @param findContentType query request parameters.
     * @return the corresponding JPA Specifications 
     */
    public static Specifications<Engagement> createQuery(FindContentType findContentType) {


        Specifications<Engagement> specs = Specifications.where(isPerson(findContentType.getRegisteredResidentIdentification()))   
                .and(hasServiceDomain(findContentType.getServiceDomain()));


        if (findContentType.getCategorization() != null) {
            specs = specs.and(hasCategorization(findContentType.getCategorization()));
        }

        if (findContentType.getMostRecentContent() != null) {
            specs = specs.and(isMostRecent(EntityTransformer.parseDate(findContentType.getMostRecentContent())));
        }

        if (findContentType.getClinicalProcessInterestId() != null) {
            specs = specs.and(hasClinicalProcessInterestId(findContentType.getClinicalProcessInterestId()));
        }

        if (findContentType.getBusinessObjectInstanceIdentifier() != null) {
            specs = specs.and(hasBusinessObjectInstanceIdentifier(findContentType.getBusinessObjectInstanceIdentifier()));            
        }

        if (findContentType.getLogicalAddress() != null) {
            specs = specs.and(hasLogicalAddress(findContentType.getLogicalAddress()));            
        }

        if (findContentType.getSourceSystem() != null) {
            specs = specs.and(hasSourceSystem(findContentType.getSourceSystem()));                        
        }

        if (findContentType.getDataController() != null) {
            specs = specs.and(hasDataController(findContentType.getDataController()));                        
        }

        if (findContentType.getOwner() != null) {
            specs = specs.and(hasOwner(findContentType.getOwner()));
        }

        return specs;

    }
    
    /**
     * Validate that we got enough information to do a query
     * 
     * @param header
     * @param findContent
     * @throws EiException
     */
    public void validateFindContent(Header header, FindContentType findContent) throws EiException {
    	 
    	// We need at least registeredResidentIdentification and serviceDomain to do a query
    	
    	if(findContent.getRegisteredResidentIdentification() == null) {
    		throw new EiException(EiErrorCodeEnum.EI000_TECHNICAL_ERROR, MISSING_PERSON_MESSAGE);
    		
    	} else if(findContent.getServiceDomain() == null) {
    		throw new EiException(EiErrorCodeEnum.EI000_TECHNICAL_ERROR, MISSING_SERVICEDOMAIN_MESSAGE);
    	} 
    }


}
