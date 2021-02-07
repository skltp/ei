package se.skltp.ei.service;

import static se.skltp.ei.entity.model.EngagementSpecifications.hasBusinessObjectInstanceIdentifier;
import static se.skltp.ei.entity.model.EngagementSpecifications.hasCategorization;
import static se.skltp.ei.entity.model.EngagementSpecifications.hasClinicalProcessInterestId;
import static se.skltp.ei.entity.model.EngagementSpecifications.hasDataController;
import static se.skltp.ei.entity.model.EngagementSpecifications.hasLogicalAddress;
import static se.skltp.ei.entity.model.EngagementSpecifications.hasOwner;
import static se.skltp.ei.entity.model.EngagementSpecifications.hasServiceDomain;
import static se.skltp.ei.entity.model.EngagementSpecifications.hasSourceSystem;
import static se.skltp.ei.entity.model.EngagementSpecifications.isMostRecent;
import static se.skltp.ei.entity.model.EngagementSpecifications.isPerson;
import static se.skltp.ei.service.util.EntityTransformer.fromEntity;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;
import se.skltp.ei.entity.model.Engagement;
import se.skltp.ei.entity.repository.EngagementRepository;
import se.skltp.ei.service.util.EntityTransformer;


@Service
public class FindContentServiceImpl implements FindContentService{

  @Autowired
  private EngagementRepository engagementRepository;

  @Override
  @Transactional(readOnly=true)
  public FindContentResponseType findContent(FindContentType parameters) {

    FindContentResponseType response = new FindContentResponseType();

    Iterable<Engagement> dbSearchResult = engagementRepository.findAll(createQuery(parameters));

    List<EngagementType> engagementList = response.getEngagement();
    for (Engagement engagement : dbSearchResult) {
      engagementList.add(fromEntity(engagement));
    }
    return response;
  }


  public static Specification<Engagement> createQuery(FindContentType findContentType) {


    Specification<Engagement> specs = Specification.where(isPerson(findContentType.getRegisteredResidentIdentification()))
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
