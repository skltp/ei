package se.skltp.ei.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.skltp.ei.service.util.EntityTransformer.toEntity;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.entity.repository.EngagementRepository;
import se.skltp.ei.util.EngagementTransactionTestUtil;

class UpdatePersistentStorageServiceImplTest {

  private static UpdatePersistentStorageServiceImpl persistentStorageService = null;
  private static final String OWNER = "logical-address";

  private static EngagementRepository mockEngagementRepository;

  @BeforeAll
  public static void setUpClass()  {
    persistentStorageService = new UpdatePersistentStorageServiceImpl();

    persistentStorageService.setOwner(OWNER);
    mockEngagementRepository = mock(EngagementRepository.class);
    persistentStorageService.setEngagementRepository(mockEngagementRepository);

  }

  @BeforeEach
  public void beforeEachTest() {
    reset(mockEngagementRepository);
  }

  @Test
  public void noEngagementsStorageNotUpdated()  {

    UpdateType request = new UpdateType();
    List<EngagementTransactionType> updatedEngagements = persistentStorageService.update(request);
    assertEquals(0, updatedEngagements.size());

    verify(mockEngagementRepository, times(0)).saveAll(any());
    verify(mockEngagementRepository, times(0)).deleteAll(any());

  }

  @Test
  public void twoUpdateEngagementsStoredCorrectly()  {

    UpdateType request = new UpdateType();
    EngagementTransactionType et1 = EngagementTransactionTestUtil.createET(1111111111L);
    EngagementTransactionType et2 = EngagementTransactionTestUtil.createET(2222222222L);

    request.getEngagementTransaction().add(et1);
    request.getEngagementTransaction().add(et2);

    List<EngagementTransactionType> resultList = persistentStorageService.update(request);
    assertEquals(2, resultList.size());

    verify(mockEngagementRepository, times(1)).saveAll(any());
    verify(mockEngagementRepository, times(0)).deleteAll(any());
  }

  @Test
  public void twoUpdateEngagementsDeletedCorrectly()  {

    UpdateType request = new UpdateType();
    EngagementTransactionType et1 = EngagementTransactionTestUtil.createET(1111111111L);
    et1.setDeleteFlag(true);
    EngagementTransactionType et2 = EngagementTransactionTestUtil.createET(2222222222L);
    et2.setDeleteFlag(true);

    request.getEngagementTransaction().add(et1);
    request.getEngagementTransaction().add(et2);

    List<EngagementTransactionType> resultList = persistentStorageService.update(request);
    assertEquals(2, resultList.size());

    verify(mockEngagementRepository, times(0)).saveAll(any());
    verify(mockEngagementRepository, times(1)).deleteAll(any());
  }

  @Test
  public void oneUpdateEngagementDeletedOneUpdatedCorrectly()  {

    UpdateType request = new UpdateType();
    EngagementTransactionType et1 = EngagementTransactionTestUtil.createET(1111111111L);
    et1.setDeleteFlag(true);
    EngagementTransactionType et2 = EngagementTransactionTestUtil.createET(2222222222L);

    request.getEngagementTransaction().add(et1);
    request.getEngagementTransaction().add(et2);

    List<EngagementTransactionType> resultList = persistentStorageService.update(request);
    assertEquals(2, resultList.size());

    verify(mockEngagementRepository, times(1)).saveAll(any());
    verify(mockEngagementRepository, times(1)).deleteAll(any());
  }

  @Test
  public void ownerIsChangedAccordingToR6ForUpdateType()  {

    UpdateType request = new UpdateType();
    EngagementTransactionType et1 = EngagementTransactionTestUtil.createET(1111111111L);
    et1.getEngagement().setOwner("SomeOtherOwner");

    request.getEngagementTransaction().add(et1);

    List<EngagementTransactionType> updatedEngagements = persistentStorageService.update(request);
    assertEquals(1, updatedEngagements.size());

    final EngagementTransactionType updatedEngagement = updatedEngagements.get(0);
    assertEquals(OWNER, updatedEngagement.getEngagement().getOwner());
  }

  @Test
  public void engagementsThatsAlreadyStoredShouldNotAppearInReturnList ()  {

    UpdateType request = new UpdateType();
    EngagementTransactionType et1 = EngagementTransactionTestUtil.createET(19740418L);
    et1.getEngagement().setOwner(OWNER);

    // Set entity as already existing in mock repository
    when(mockEngagementRepository.findByIdIn(any())).thenReturn(Arrays.asList(toEntity(et1.getEngagement())));

    request.getEngagementTransaction().add(et1);

    List<EngagementTransactionType> updatedEngagements = persistentStorageService.update(request);
    assertEquals(0, updatedEngagements.size());

  }



  @Test
  public void twoProcessNotificationEngagementsStoredCorrectly()  {

    ProcessNotificationType request = new ProcessNotificationType();
    EngagementTransactionType et1 = EngagementTransactionTestUtil.createET(1111111111L);
    EngagementTransactionType et2 = EngagementTransactionTestUtil.createET(2222222222L);

    request.getEngagementTransaction().add(et1);
    request.getEngagementTransaction().add(et2);

    List<EngagementTransactionType> resultList = persistentStorageService.update(request);
    assertEquals(2, resultList.size());

    verify(mockEngagementRepository, times(1)).saveAll(any());
    verify(mockEngagementRepository, times(0)).deleteAll(any());
  }

  @Test
  public void twoProcessNotificationEngagementsDeletedCorrectly()  {

    ProcessNotificationType request = new ProcessNotificationType();
    EngagementTransactionType et1 = EngagementTransactionTestUtil.createET(1111111111L);
    et1.setDeleteFlag(true);
    EngagementTransactionType et2 = EngagementTransactionTestUtil.createET(2222222222L);
    et2.setDeleteFlag(true);

    request.getEngagementTransaction().add(et1);
    request.getEngagementTransaction().add(et2);

    List<EngagementTransactionType> resultList = persistentStorageService.update(request);
    assertEquals(2, resultList.size());

    verify(mockEngagementRepository, times(0)).saveAll(any());
    verify(mockEngagementRepository, times(1)).deleteAll(any());
  }

  @Test
  public void processNotificationEngagementsWithNewOwnerShouldRemoveExistingAccordingToR5()  {

    // Set entity as already existing in mock repository
    EngagementTransactionType existing = EngagementTransactionTestUtil.createET(5555555555L);
    existing.getEngagement().setOwner(OWNER);
    when(mockEngagementRepository.findByIdIn(any())).thenReturn(Arrays.asList(toEntity(existing.getEngagement())));

    // Update owner with ProcessNotification
    ProcessNotificationType request = new ProcessNotificationType();
    EngagementTransactionType newEngagement = EngagementTransactionTestUtil.createET(5555555555L);
    newEngagement.getEngagement().setOwner("NEW_OWNER");
    request.getEngagementTransaction().add(newEngagement);

    List<EngagementTransactionType> resultList = persistentStorageService.update(request);
    assertEquals(1, resultList.size());

    // Verify old owner removed and new saved
    verify(mockEngagementRepository, times(1)).saveAll(argThat( saveList -> {
      assertTrue(saveList.iterator().hasNext());
      assertEquals("NEW_OWNER", saveList.iterator().next().getOwner());
      return true;
    }));
    verify(mockEngagementRepository, times(1)).deleteAll(argThat( deleteList -> {
      assertTrue(deleteList.iterator().hasNext());
      assertEquals(OWNER, deleteList.iterator().next().getOwner());
      return true;
    }));
  }



}