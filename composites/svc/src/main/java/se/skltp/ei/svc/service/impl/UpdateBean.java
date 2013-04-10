package se.skltp.ei.svc.service.impl;

import static se.skltp.ei.svc.service.impl.util.EntityTransformer.toEntity;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.api.Header;
import se.skltp.ei.svc.service.api.UpdateInterface;

public class UpdateBean implements UpdateInterface {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateBean.class);

    private EngagementRepository engagementRepository;

    @Autowired
    public void setEngagementRepository(EngagementRepository engagementRepository) {
    	this.engagementRepository = engagementRepository;
    }

    /**
     *
     * @param header
     * @param request
     * @return
     */
    @Override
    public UpdateResponseType update(Header header, UpdateType request) {
    	LOG.debug("The svc.update service is called");

    	List<EngagementTransactionType> engagementTransactions = request.getEngagementTransaction();

    	// Construct a list of entities to be saved
    	List<Engagement> entities = new ArrayList<Engagement>();
    	for (EngagementTransactionType engagementTransaction : engagementTransactions) {
    		entities.add(toEntity(engagementTransaction.getEngagement()));
		}

    	// Save the list in one operation
    	engagementRepository.save(entities);

    	// Create a response
        UpdateResponseType response = new UpdateResponseType();
        response.setComment(null);
        response.setResultCode(ResultCodeEnum.OK);
        return response;
    }
}