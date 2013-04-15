package se.skltp.ei.svc.service.impl;

import static se.skltp.ei.svc.service.impl.util.EntityTransformer.toEntity;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

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

    	List<EngagementTransactionType> engagementTransactions = request.getEngagementTransaction();

    	// Construct a list of entities to be saved
    	List<Engagement> entities = new ArrayList<Engagement>();
    	for (EngagementTransactionType engagementTransaction : engagementTransactions) {
    		Engagement e = toEntity(engagementTransaction.getEngagement());
    		e.setDeleteFlag(engagementTransaction.isDeleteFlag());
    		entities.add(e);
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