package se.skltp.ei.svc.service.impl.util;

import org.mule.util.Preconditions;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.model.util.Hash;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.impl.ProcessBean;

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Convenience methods, that mainly returns tests that check different aspects of specific Engagement data after it been
 * included in a call to ProcessBean method (update or processNotification). And for method for execution of these tests
 *
 */
public class ProcessBeanIntegrationTestHelper {

    private static String defaultMostRecentContent;

    static {
        defaultMostRecentContent = EntityTransformer.formatDate(new Date());
    }


    private static Engagement engagementByEngagementTransactionType(List<Engagement> pSource, TestDataHelper.TestDataEnums key) {
        for (Engagement engagement : pSource) {

            if (engagement.getId().equals(key.getLogicalId())) {
                return engagement;
            }
        }
        return null;
    }


    public static void incMostRecentDate(int incBy, TestDataHelper.TestDataEnums... engagementTypes) {
        for (TestDataHelper.TestDataEnums et : engagementTypes) {
            String current = ((current = et.getEt().getEngagement().getMostRecentContent()) != null) ? current : defaultMostRecentContent;
            et.getEt().getEngagement().setMostRecentContent(
                    EntityTransformer.dateDaysFromStrDate(current
                            , incBy));
        }
    }

    private static EngagementTransactionType findCorrespondingEngagementTransactionType(Iterable<EngagementTransactionType> candidates, TestDataHelper.TestDataEnums key) {
        EngagementTransactionType result = null;
        for (EngagementTransactionType engagementTransactionType : candidates) {

            if (Hash.generateHashId(EntityTransformer.toEntity(engagementTransactionType.getEngagement())).equals(key.getLogicalId())) {
                result = engagementTransactionType;
                break;
            }
        }
        return result;
    }

    public static EngagementReturnedAsExpected resultDateEqualsPreProcessDate(TestDataHelper.TestDataEnums candidate) {
        return new EngagementReturnedAsExpected() {
            String message = "";

            @Override
            public boolean isTrueFor(List<EngagementTransactionType> processResult) {
                EngagementTransactionType found = findCorrespondingEngagementTransactionType(processResult, candidate);

                if (found == null) {
                    message = "dateEquals " + candidate.name() + "was not a part of the result set equality undefined";
                    return false;
                }
                if (found.getEngagement().getMostRecentContent() == null) {
                    if (candidate.getPreProcessMostRecentContent() == null) {
                        return true;
                    } else {
                        message = "dateEquals " + candidate.name() + ".PreProcessMostRecentContent: " + candidate.getPreProcessMostRecentContent()
                                + " differ from " + candidate.name() + " post process date: " + candidate.getEt().getEngagement().getMostRecentContent();
                        return false;
                    }
                }
                if (!found.getEngagement().getMostRecentContent().equals(candidate.getPreProcessMostRecentContent())) {
                    message = "dateEquals " + candidate.name() + ".PreProcessMostRecentContent: " + candidate.getPreProcessMostRecentContent()
                            + " differ from " + candidate.name() + " post process date: " + candidate.getEt().getEngagement().getMostRecentContent();
                    return false;
                }
                return true;
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }



    public static EngagementReturnedAsExpected notInResultSet(TestDataHelper.TestDataEnums candidate) {
        return new EngagementReturnedAsExpected() {
            String message = "";

            @Override
            public boolean isTrueFor(List<EngagementTransactionType> processResult) {
                EngagementTransactionType found = findCorrespondingEngagementTransactionType(processResult, candidate);
                if (found != null) {

                    message = "notInResultSet " + candidate.name() + " was indeed included in the result";
                }
                return found == null;
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }

    public static EngagementReturnedAsExpected isInResultSet(TestDataHelper.TestDataEnums candidate) {
        return new EngagementReturnedAsExpected() {
            String message = "";

            @Override
            public boolean isTrueFor(List<EngagementTransactionType> processResult) {
                EngagementTransactionType found = findCorrespondingEngagementTransactionType(processResult, candidate);
                if (found == null) {

                    message = "isInResultSet " + candidate.name() + " was not included in the result";
                }
                return found != null;
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }


    public static EngagementReturnedAsExpected resultSize(int expected) {
        return new EngagementReturnedAsExpected() {
            String message = "";

            @Override
            public boolean isTrueFor(List<EngagementTransactionType> processResult) {
                if (processResult.size() != expected) {
                    message = "resultSize: " + processResult.size() + " not as expected: " + expected;
                    return false;
                }
                return true;
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }

    /**
     * returns an implementation that checks if there exists a matching persisted engagement with a MostRecentContent equal
     * to that of the candidate
     *
     * @param candidate the candidate for evaluation
     * @return suitable implementation of EngagementPersistedAsExpected
     */
    public static EngagementPersistedAsExpected dateEqualsPersisted(TestDataHelper.TestDataEnums candidate) {
        return new EngagementPersistedAsExpected() {
            String message = "";

            @Override
            public boolean isTrueFor(List<Engagement> persistedEngagements) {
                Engagement persisted = engagementByEngagementTransactionType(persistedEngagements, candidate);
                if (persisted == null) {
                    message = "dateEqualsPersisted candidate: " + candidate + " is not persisted";
                    return false;
                }
                Date persistedDate = persisted.getMostRecentContent();
                Date candidateDate = EntityTransformer.parseDate(candidate.getEt().getEngagement().getMostRecentContent());

                if (Objects.equals(candidateDate, persistedDate)) {
                    return true;
                } else {
                    message = "dateEqualsPersisted candidate.MostRecentContent:" + candidateDate + "<> persisted.MostRecentConten" + persistedDate + " " + candidate;
                    return false;
                }

            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }


    public static EngagementPersistedAsExpected engagementIsPersisted(TestDataHelper.TestDataEnums candidate) {
        return new EngagementPersistedAsExpected() {
            String message = "";

            @Override
            public boolean isTrueFor(List<Engagement> persistedEngagements) {
                Engagement persisted = engagementByEngagementTransactionType(persistedEngagements, candidate);

                if (persisted == null) {
                    message = "engagementIsPersisted is false for candidate: " + candidate;
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }

    /**
     * @param candidate the candidate for evaluation
     * @return test
     */
    public static EngagementPersistedAsExpected persistedMostRecentContentIsNull(TestDataHelper.TestDataEnums candidate) {
        return new EngagementPersistedAsExpected() {
            String message = "";

            @Override
            public boolean isTrueFor(List<Engagement> persistedEngagements) {
                Engagement persisted = engagementByEngagementTransactionType(persistedEngagements, candidate);
                if (persisted == null) {
                    message = "persistedMostRecentContentIsNull candidate: " + candidate.name() + " is not persisted";
                    return false;
                }

                if (persisted.getMostRecentContent() == null) {
                    return true;
                } else {
                    message = "persistedMostRecentContentIsNull matching " + candidate.name() +
                            "persisted.MostRecentContent: " + persisted.getMostRecentContent() + "is not null";
                    return false;
                }

            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }


    /**
     * Conducts all given tests and returns any problems that the tests may detect as a string.
     *
     * @param processResult        result of invoking process
     * @param engagementRepository persistent layer
     * @param tests                bound to a specific candidate and conditions
     *                             that's either of type  EngagementReturnedAsExpected or EngagementPersistedAsExpected
     * @return blank if all test where true otherwise a description of what condition wasn't matched by candidate
     */
    public static String checkTest(List<EngagementTransactionType> processResult, EngagementRepository engagementRepository, TrialExitMessage... tests) {

        Preconditions.checkArgument(processResult != null, "ProcessResult must be assigned");
        Preconditions.checkArgument(engagementRepository != null, "EngagementRepository must be assigned");
        StringBuilder buff = new StringBuilder();

        List<Engagement> engagements = engagementRepository.findAll(); //engagementRepository.findByIdIn(TestDataHelper.getEnumLogicalIds());
        for (TrialExitMessage test : tests) {
            testAsEither(processResult, engagements, test, buff);
        }
        return buff.toString();
    }

    private static void testAsEither(List<EngagementTransactionType> processResult,
                                     List<Engagement> engagements,
                                     TrialExitMessage test,
                                     StringBuilder buff) {
        if (
                testIsOnResultAndInvalid(processResult, test)
                        ||
                        testIsOnPersistedAndInvalid(engagements, test)
        ) {
            buff.append(test.getMessage());
        }
    }

    private static boolean testIsOnResultAndInvalid(List<EngagementTransactionType> processResult, TrialExitMessage test) {
        return (test instanceof EngagementReturnedAsExpected) &&
                (!((EngagementReturnedAsExpected) test).isTrueFor(processResult));
    }

    private static boolean testIsOnPersistedAndInvalid(List<Engagement> engagements, TrialExitMessage test) {
        return (test instanceof EngagementPersistedAsExpected) &&
                (!((EngagementPersistedAsExpected) test).isTrueFor(engagements));
    }

    /**
     * @param candidate the candidate for evaluation
     * @return test
     */
    public static EngagementPersistedAsExpected engagementIsNotPersisted(TestDataHelper.TestDataEnums candidate) {
        return new EngagementPersistedAsExpected() {
            String message = "";

            @Override
            public boolean isTrueFor(List<Engagement> persistedEngagements) {
                Engagement persisted = engagementByEngagementTransactionType(persistedEngagements, candidate);

                if (persisted != null) {
                    message = "engagementIsNotPersisted candidate: " + candidate + " has a persisted engagement:" + persisted;
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }

    /**
     * @param candidate the candidate for evaluation
     * @return test
     */
    public static EngagementPersistedAsExpected preResetIdRemovedFromPersisted(TestDataHelper.TestDataEnums candidate) {
        return new EngagementPersistedAsExpected() {
            String message = "";
            @Override
            public boolean isTrueFor(List<Engagement> persistedEngagements) {
                for (Engagement engagement : persistedEngagements) {
                    if (engagement.getId().equals(candidate.getPreResetLogicalId())) {
                        message = "The pre reset id: " + candidate.getPreResetLogicalId() + " exists among the persisted: " + engagement;
                        return false;
                    }
                }
                return true;
            }
            @Override
            public String getMessage() {
                return message;
            }
        };
    }

    /**
     * Test checks if there exists a matching persisted engagement where MostRecentContent is a date after that of the
     * given candidate
     *
     * @param candidate the candidate for evaluation
     * @return suitable implementation of EngagementPersistedAsExpected
     */
    public static EngagementPersistedAsExpected datePersistedIsAfter(TestDataHelper.TestDataEnums candidate) {
        return new EngagementPersistedAsExpected() {
            String message = "";

            @Override
            public boolean isTrueFor(List<Engagement> persistedEngagements) {
                Engagement persisted = engagementByEngagementTransactionType(persistedEngagements, candidate);

                if (persisted == null) {
                    message = "datePersistedIsAfter candidate: " + candidate + " is not persisted";
                    return false;
                } else {
                    if (persisted.getMostRecentContent() == null) {
                        message = "datePersistedIsAfter candidate: " + candidate + " persisted date is null";
                        return false;
                    }

                    Date candidateDate = EntityTransformer.parseDate(candidate.getEt().getEngagement().getMostRecentContent());

                    if (candidateDate == null) {//This is treated as if new date is before
                        return true;
                    }
                    if (candidateDate.after(persisted.getMostRecentContent())) {
                        message = "datePersistedIsAfter persisted.MostRecentContent: " + persisted.getMostRecentContent() +
                                " is not after  candidate.MostRecentContent: " + candidateDate;
                        return false;
                    }
                    return true;
                }
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }

    /**
     * Convenience method for getting getting the owner for an engagement in a list based on getRegisteredResidentIdentification
     *
     * @param result candidates
     * @param et     key et.ResidentIdentification
     * @return candidate.owner matching key
     */
    public static String getOwner(List<Engagement> result, EngagementTransactionType et) {
        for (Engagement e : result) {
            if (e.getRegisteredResidentIdentification().equals(et.getEngagement().getRegisteredResidentIdentification())) {
                return e.getOwner();
            }
        }

        return "";
    }


    /**
     * Convenience method for getting the only saved Engagement from the data store. Asserts that it only finds one Engagement
     *
     * @return Engagement
     */
    public static Engagement getSingleEngagement(EngagementRepository engagementRepository) {
        engagementRepository.flush();
        List<Engagement> result = engagementRepository.findAll();
        assertThat(result, hasSize(1));

        return result.get(0);
    }

    public static int countEngagements(EngagementRepository engagementRepository) {
        engagementRepository.flush();
        List<Engagement> result = engagementRepository.findAll();
        return result.size();
    }

    public static List<EngagementTransactionType> updateOrProcessNotification(ProcessBean processHandler, Object request) {
        TestDataHelper.preProcessAll();
        if (request instanceof UpdateType) {
            return processHandler.update(null, (UpdateType) request);
        } else {
            return processHandler.processNotification(null, (ProcessNotificationType) request);
        }
    }
}
