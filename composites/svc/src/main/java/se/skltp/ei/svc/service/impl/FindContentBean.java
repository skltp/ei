package se.skltp.ei.svc.service.impl;

import static se.skltp.ei.svc.service.impl.util.EntityTransformer.fromEntity;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.api.FindContentInterface;
import se.skltp.ei.svc.service.api.Header;

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
    	
    	Iterable<Engagement> dbSearchResult = engagementRepository.findAll();
    	
    	List<EngagementType> EngagementList = response.getEngagement();
    	for (Engagement engagement : dbSearchResult) {
			EngagementList.add(fromEntity(engagement));
		}
        return response;
    }

}
