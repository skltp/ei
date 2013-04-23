package se.skltp.ei.svc.service.impl;

import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI002_DUPLICATE_UPDATE_ENTRIES;
import static se.skltp.ei.svc.service.impl.util.EntityTransformer.toEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import se.skltp.ei.svc.service.api.EiException;
import se.skltp.ei.svc.service.api.Header;
import se.skltp.ei.svc.service.api.ProcessInterface;

public class ProcessBean implements ProcessInterface {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessBean.class);

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
     * Validates an update request without touching the database.
     * 
     * @param header the header
     * @param request the request
     */
    @Override
    public void validateUpdate(Header header, UpdateType request) {
       
        final Map<String, Integer> hashCodes = new HashMap<String, Integer>(request.getEngagementTransaction().size());
        int hashCodeIndex = 0;

        for (final EngagementTransactionType engagementTransaction : request.getEngagementTransaction()) {
            final Engagement e = toEntity(engagementTransaction.getEngagement());
            // Update, R1: Validate uniqueness within the request
            final Integer otherIndex = hashCodes.put(e.getBusinessKey().getHashId(), ++hashCodeIndex);
            if (otherIndex != null) {
            	throw new EiException(EI002_DUPLICATE_UPDATE_ENTRIES, otherIndex, hashCodeIndex);
            }
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

        // Perform the delete if any
        if (deleteList != null) {
            engagementRepository.delete(deleteList);
        }

        // Perform the save
        engagementRepository.save(saveList);  	

        return RESPONSE_OK;
    }
}