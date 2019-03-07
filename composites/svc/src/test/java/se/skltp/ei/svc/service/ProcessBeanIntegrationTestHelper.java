package se.skltp.ei.svc.service;

import org.mule.util.Preconditions;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.model.util.Hash;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.impl.ProcessBean;
import se.skltp.ei.svc.service.impl.util.EntityTransformer;

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Convenience methods ProcessBeanIntegrationTest
 */
class ProcessBeanIntegrationTestHelper {

    private static String defaultMostRecentContent;

    static {
        defaultMostRecentContent = EntityTransformer.formatDate(new Date());
    }


    static Engagement engagementByEngagementTransactionType(List<Engagement> pSource, TestDataHelper.TestDataEnums key) {
        for (Engagement engagement : pSource) {

            if (engagement.getId().equals(key.getLogicalId())) {
                return engagement;
            }
        }
        return null;
    }


    static void incMostRecentDate(int incBy, TestDataHelper.TestDataEnums... engagementTypes) {
        for (TestDataHelper.TestDataEnums et : engagementTypes) {
            String current = ((current = et.getEt().getEngagement().getMostRecentContent()) != null) ? current : defaultMostRecentContent;
            et.getEt().getEngagement().setMostRecentContent(
                    EntityTransformer.dateDaysFromStrDate(current
                            , incBy));
        }
    }

    static EngagementTransactionType findCorrespondingEngagementTransactionType(Iterable<EngagementTransactionType> candidates, TestDataHelper.TestDataEnums key) {
        EngagementTransactionType result = null;
        for (EngagementTransactionType engagementTransactionType : candidates) {

            if (Hash.generateHashId(EntityTransformer.toEntity(engagementTransactionType.getEngagement())).equals(key.getLogicalId())) {
                result = engagementTransactionType;
                break;
            }
        }
        return result;
    }

    static MostRecentContentReturnedAsExpected resultDateEqualsPreProcessDate(TestDataHelper.TestDataEnums candidate) {
        return new MostRecentContentReturnedAsExpected() {
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

    static MostRecentContentReturnedAsExpected resultDateNotEquals(TestDataHelper.TestDataEnums candidate) {
        return new MostRecentContentReturnedAsExpected() {
            String message = "";

            @Override
            public boolean isTrueFor(List<EngagementTransactionType> processResult) {
                EngagementTransactionType found = findCorrespondingEngagementTransactionType(processResult, candidate);
                if (found == null) {
                    message = "dateEquals " + candidate.name() + "was not a part of the result set equality undefined";
                    return false;
                }
                if (found.getEngagement().getMostRecentContent().equals(candidate.getPreProcessMostRecentContent())) {
                    message = "dateEquals " + candidate.name() + ".PreProcessMostRecentContent: " + candidate.getPreProcessMostRecentContent()
                            + " is the same as " + candidate.name() + " post process date: " + candidate.getEt().getEngagement().getMostRecentContent();
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

    static MostRecentContentReturnedAsExpected notInResultSet(TestDataHelper.TestDataEnums candidate) {
        return new MostRecentContentReturnedAsExpected() {
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

    static MostRecentContentReturnedAsExpected isInResultSet(TestDataHelper.TestDataEnums candidate) {
        return new MostRecentContentReturnedAsExpected() {
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


    static MostRecentContentReturnedAsExpected resultSize(int expected) {
        return new MostRecentContentReturnedAsExpected() {
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
     * @return suitable implementation of MostRecentContentPersistedAsExpected
     */
    static MostRecentContentPersistedAsExpected dateEqualsPersisted(TestDataHelper.TestDataEnums candidate) {
        return new MostRecentContentPersistedAsExpected() {
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


    static MostRecentContentPersistedAsExpected engagementIsPersisted(TestDataHelper.TestDataEnums candidate) {
        return new MostRecentContentPersistedAsExpected() {
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
     * returns an implementation that checks if there exists a matching persisted engagement where MostRecentContent
     * is null
     *
     * @param candidate the candidate for evaluation
     * @return suitable implementation of MostRecentContentPersistedAsExpected
     */
    static MostRecentContentPersistedAsExpected persistedDateIsNull(TestDataHelper.TestDataEnums candidate) {
        return new MostRecentContentPersistedAsExpected() {
            String message = "";

            @Override
            public boolean isTrueFor(List<Engagement> persistedEngagements) {
                Engagement persisted = engagementByEngagementTransactionType(persistedEngagements, candidate);
                if (persisted == null) {
                    message = "persistedDateIsNull candidate: " + candidate.name() + " is not persisted";
                    return false;
                }

                if (persisted.getMostRecentContent() == null) {
                    return true;
                } else {
                    message = "persistedDateIsNull matching " + candidate.name() +
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


    static String checkMostRecentContentPersisted(EngagementRepository engagementRepository, MostRecentContentPersistedAsExpected... tests) {
        StringBuilder buff = new StringBuilder();

        List<Engagement> engagements = engagementRepository.findByIdIn(TestDataHelper.getEnumLogicalIds());
        for (MostRecentContentPersistedAsExpected mostRecentContentPersistedAsExpected : tests) {
            if (!mostRecentContentPersistedAsExpected.isTrueFor(engagements)) {
                buff.append(mostRecentContentPersistedAsExpected.getMessage());
            }
        }
        return buff.toString();
    }

    /**
     * Conducts all given tests and returns any problems that the tests may detect as a string.
     *
     * @param processResult        result of invoking process
     * @param engagementRepository persistent layer
     * @param tests                bound to a specific candidate and conditions
     *                             that's either of type  MostRecentContentReturnedAsExpected or MostRecentContentPersistedAsExpected
     * @return blank if all test where true otherwise a description of what condition wasn't matched by candidate
     */
    static String checkTest(List<EngagementTransactionType> processResult, EngagementRepository engagementRepository, CommonProcessTest... tests) {

        Preconditions.checkArgument(processResult != null, "ProcessResult must be assigned");
        Preconditions.checkArgument(engagementRepository != null, "EngagementRepository must be assigned");
        StringBuilder buff = new StringBuilder();
        List<Engagement> engagements = engagementRepository.findByIdIn(TestDataHelper.getEnumLogicalIds());
        for (CommonProcessTest test : tests) {
            testAsEither(processResult, engagements, test, buff);
        }
        return buff.toString();
    }

    private static void testAsEither(List<EngagementTransactionType> processResult,
                                     List<Engagement> engagements,
                                     CommonProcessTest test,
                                     StringBuilder buff) {
        if (
                testIsOnResultAndInvalid(processResult, test)
                        ||
                        testIsOnPersistedAndInvalid(engagements, test)
        ) {
            buff.append(test.getMessage());
        }
    }

    private static boolean testIsOnResultAndInvalid(List<EngagementTransactionType> processResult, CommonProcessTest test) {
        return (test instanceof MostRecentContentReturnedAsExpected) &&
                (!((MostRecentContentReturnedAsExpected) test).isTrueFor(processResult));
    }

    private static boolean testIsOnPersistedAndInvalid(List<Engagement> engagements, CommonProcessTest test) {
        return (test instanceof MostRecentContentPersistedAsExpected) &&
                (!((MostRecentContentPersistedAsExpected) test).isTrueFor(engagements));
    }

    /**
     * returns an implementation that checks that there not exists a matching persisted engagement
     *
     * @param candidate the candidate for evaluation
     * @return suitable implementation of MostRecentContentPersistedAsExpected
     */
    static MostRecentContentPersistedAsExpected engagementIsNotPersisted(TestDataHelper.TestDataEnums candidate) {
        return new MostRecentContentPersistedAsExpected() {
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
     * returns an implementation that checks if there exists a matching persisted engagement where MostRecentContent
     * is a date after that of the given candidate
     *
     * @param candidate the candidate for evaluation
     * @return suitable implementation of MostRecentContentPersistedAsExpected
     */
    static MostRecentContentPersistedAsExpected datePersistedIsAfter(TestDataHelper.TestDataEnums candidate) {
        return new MostRecentContentPersistedAsExpected() {
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
    static String getOwner(List<Engagement> result, EngagementTransactionType et) {
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
    static Engagement getSingleEngagement(EngagementRepository engagementRepository) {
        engagementRepository.flush();
        List<Engagement> result = engagementRepository.findAll();
        assertThat(result, hasSize(1));

        return result.get(0);
    }

    static int countEngagements(EngagementRepository engagementRepository) {
        engagementRepository.flush();
        List<Engagement> result = engagementRepository.findAll();
        return result.size();
    }

    static List<EngagementTransactionType> updateOrProcessNotification(ProcessBean processHandler, Object request) {
        TestDataHelper.preProcessAll();
        if (request instanceof UpdateType) {
            return processHandler.update(null, (UpdateType) request);
        } else {
            return processHandler.processNotification(null, (ProcessNotificationType) request);
        }
    }
}
