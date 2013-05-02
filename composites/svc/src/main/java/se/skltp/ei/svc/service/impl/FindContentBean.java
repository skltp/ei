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
import se.skltp.ei.svc.service.api.FindContentInterface;
import se.skltp.ei.svc.service.api.Header;
import se.skltp.ei.svc.service.impl.util.EntityTransformer;

public class FindContentBean implements FindContentInterface {

    private static final Logger LOG = LoggerFactory.getLogger(FindContentBean.class);

    private EngagementRepository engagementRepository;

    @Autowired
    public void setEngagementRepository(EngagementRepository engagementRepository) {
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

}
