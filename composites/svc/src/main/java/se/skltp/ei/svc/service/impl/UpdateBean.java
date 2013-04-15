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

    //
    private static UpdateResponseType RESPONSE_OK = new UpdateResponseType() {
        @Override
        public ResultCodeEnum getResultCode() {
            return ResultCodeEnum.OK;
        }
    };

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

        final List<EngagementTransactionType> engagementTransactions = request.getEngagementTransaction();
        final List<Engagement> saveList = new ArrayList<Engagement>(engagementTransactions.size());
        List<Engagement> deleteList = null;
        for (final EngagementTransactionType engagementTransaction : engagementTransactions) {
            final Engagement e = toEntity(engagementTransaction.getEngagement());
            if (engagementTransaction.isDeleteFlag()) {
                if (deleteList == null) {
                    deleteList = new ArrayList<Engagement>();
                }
                deleteList.add(e);
            } else {
                saveList.add(e);
            }
        }

        if (deleteList != null) {
            engagementRepository.delete(deleteList);
        }
        engagementRepository.save(saveList);  	

        return RESPONSE_OK;
    }
}