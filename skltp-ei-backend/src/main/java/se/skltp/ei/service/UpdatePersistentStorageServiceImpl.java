package se.skltp.ei.service;

import static se.skltp.ei.service.util.EntityTransformer.formatDate;
import static se.skltp.ei.service.util.EntityTransformer.toEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.entity.model.Engagement;
import se.skltp.ei.entity.repository.EngagementRepository;

@Service
@Log4j2
@Data
public class UpdatePersistentStorageServiceImpl implements UpdatePersistentStorageService {

  @Autowired
  private EngagementRepository engagementRepository;

  @Value("${ei.hsa.id}")
  private String owner;

  /**
   * Due to the fact that no underlying XA resources is in use, this transaction will be completely standalone and not to be
   * confused with other transactions, i.e. already started JMS transactions.
   */
  @Override
  @Transactional(isolation = Isolation.READ_UNCOMMITTED)
  public List<EngagementTransactionType> update(UpdateType request) {
    log.debug("update database service is called");

    // Separate deletes from the saves...
    final List<Engagement> saveList = new ArrayList();
    List<Engagement> deleteList = new ArrayList();
    for (final EngagementTransactionType engagementTransaction : request.getEngagementTransaction()) {

      EngagementType engagementType = engagementTransaction.getEngagement();
      engagementType.setOwner(this.owner); // According to R6 owner should always be set to owner of the index

      final Engagement e = toEntity(engagementType);
      if (engagementTransaction.isDeleteFlag()) {
        deleteList.add(e);
      } else {
        saveList.add(e);
      }
    }

    // Get notificationlist before any change is done
    final List<EngagementTransactionType> allChangedEngagementTransactions = getAllChangedEngagementTransactions(request);

    if (!deleteList.isEmpty()) {
      engagementRepository.deleteAll(deleteList);
    }

    if (!saveList.isEmpty()) {
      engagementRepository.saveAll(saveList);
    }

    // Return a list of EngagementTransactions for notification to subscribers
    return allChangedEngagementTransactions;
  }


  @Override
  @Transactional(isolation=Isolation.READ_UNCOMMITTED)
  public List<EngagementTransactionType> update(ProcessNotificationType request) {
    log.debug("Update database from processNotification called");

    final List<Engagement> saveList = new ArrayList();
    List<Engagement> deleteList = new ArrayList();

    // Separate deletes from the saves...
    for (final EngagementTransactionType engagementTransaction : request.getEngagementTransaction()) {
      final Engagement e = toEntity(engagementTransaction.getEngagement());
      if (engagementTransaction.isDeleteFlag()) {
        deleteList.add(e);
      } else {
        saveList.add(e);
      }
    }


    // R5 - fetch all posts that should be removed since the incoming Engagement changed owner
    List<Engagement> engagementsWithNewOwners = getEngagementsWithNewOwners(request);
    if(!engagementsWithNewOwners.isEmpty()) {
      for(final Engagement e : engagementsWithNewOwners) {
        log.warn("The owner has changed for Engagement with ID: " +  e.getId());
        deleteList.add(e);
      }
    }

    if (!deleteList.isEmpty()) {
      engagementRepository.deleteAll(deleteList);
    }

    if (!saveList.isEmpty()) {
      engagementRepository.saveAll(saveList);
    }

    // Return a list of EngagementTransactions for now, we will remove duplicate add/updates later
    return request.getEngagementTransaction();
  }

  /**
   * R5 - fetch all posts that should be removed since the incoming Engagement changed owner
   *
   * This method is not part of the public API
   *
   */
  private List<Engagement> getEngagementsWithNewOwners(ProcessNotificationType request) {

    final List<String> ids = new ArrayList();
    for (final EngagementTransactionType engagementTransaction : request.getEngagementTransaction()) {
      ids.add(toEntity(engagementTransaction.getEngagement(), this.owner).getId());
    }

    // The request fail if findByIdIn receives an empty list
    if (ids.isEmpty()) {
      return Collections.emptyList();
    } else {
      return engagementRepository.findByIdIn(ids);
    }
  }



  private List<EngagementTransactionType> getAllChangedEngagementTransactions(UpdateType request) {
    // Create a list of EngagementTransactions that we will use as a base for Notifications
    final List<EngagementTransactionType> notificationTransactions = new ArrayList();

    // Get a hashmap with data for most_recent_content
    Map<String, Engagement> existingContent = getEngagementsThatExistsWithContent(request);

    // Loop over our incoming list of data and move data to new list if a notification should be sent
    for (final EngagementTransactionType newEngagementTransaction : request.getEngagementTransaction()) {

      EngagementType inET = newEngagementTransaction.getEngagement();
      inET.setOwner(this.owner); // According to R6 owner should always be set to owner of the index
      Engagement newEngagement = toEntity(inET);

      boolean originalEngagementExists = existingContent.containsKey(newEngagement.getId());
      Engagement originalEngagement = originalEngagementExists ? existingContent.get(newEngagement.getId()) : null;

      // We must set a creationTime and updateTime for notifications.
      Date creationTime = originalEngagement != null ? originalEngagement.getCreationTime() : null;
      if (creationTime != null) {
        inET.setUpdateTime(formatDate(new Date()));
      }
      inET.setCreationTime(formatDate(creationTime == null ? new Date() : creationTime));

      // If delete flag is set add it to notificationList
      if (newEngagementTransaction.isDeleteFlag() || !originalEngagementExists || isMostRecentContentChanged(originalEngagement, newEngagement) ) {
        notificationTransactions.add(newEngagementTransaction);
      }
    }
    return notificationTransactions;
  }

  private boolean isMostRecentContentChanged(Engagement originalEngagement, Engagement newEngagement ){
    String newMostrecentContent = newEngagement.getMostRecentContent() == null ? "null" : formatDate(newEngagement.getMostRecentContent());
    String originalMostRecentContent = (originalEngagement == null || originalEngagement.getMostRecentContent() == null) ? "null"
        : formatDate(originalEngagement.getMostRecentContent());

    return !newMostrecentContent.equalsIgnoreCase(originalMostRecentContent);
  }

  public Map<String, Engagement> getEngagementsThatExistsWithContent(UpdateType request) {

    final List<EngagementTransactionType> engagementTransactions = request.getEngagementTransaction();
    final List<String> ids = new ArrayList(engagementTransactions.size());

    for (final EngagementTransactionType engagementTransaction : engagementTransactions) {
      EngagementType et = engagementTransaction.getEngagement();
      ids.add(toEntity(et, this.owner).getId());
    }

    // The request fail if findByIdIn receives an empty list
    if (ids.isEmpty()) {
      return Collections.emptyMap();
    } else {
      // Create a new HashMap with id as key and most_recent_time as value
      HashMap<String, Engagement> returnMap = new HashMap();
      for (Engagement engagement : engagementRepository.findByIdIn(ids)) {
        returnMap.put(engagement.getId(), engagement);
      }
      return returnMap;
    }
  }
}
