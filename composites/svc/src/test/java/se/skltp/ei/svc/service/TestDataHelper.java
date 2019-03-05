package se.skltp.ei.svc.service;


import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.model.util.Hash;
import se.skltp.ei.svc.service.api.Header;
import se.skltp.ei.svc.service.impl.ProcessBean;
import se.skltp.ei.svc.service.impl.util.EntityTransformer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

class TestDataHelper {
    /**
     * Holds a number of enums that wraps EngagementTransactionType for test purposes. Note that neither the Enums them
     * self nor the wrapped test data is immutable (usually enums are used as immutable but there is nothing wrong with
     * changing their state).
     * <p>
     * However the implementation of getLogicalId ensures that: the processes called by the Tests don't change any data
     * that is a part of the key. Eg. process may change dates or choose not to include it in the result if the data
     * did not result in any change of state (ignores the data), but must keep the integrity of the engagement.
     *
     * @see #getLogicalId()
     */
    enum TestDataEnums {
        MR_PINK_1(GenServiceTestDataUtil.genEngagementTransaction(1111111111L), "Mr-Pink(1)"),
        MR_BROWN_2(GenServiceTestDataUtil.genEngagementTransaction(2222222222L), "Mr-Brown(2)"),
        MS_SALLY_3(GenServiceTestDataUtil.genEngagementTransaction(3333333333L), "Ms-Sally(3)");
        private EngagementTransactionType et;

        private String logicalId;

        private String name;

        /**
         * @return MostRecentContent value before process
         * @see #preProcess()
         * @see se.skltp.ei.svc.service.ProcessBeanIntegrationTestHelper#updateOrProcessNotification(ProcessBean, Object)
         */
        public String getPreProcessMostRecentContent() {
            return preProcessMostRecentContent;
        }

        private String preProcessMostRecentContent;

        /**
         * Since tests share memory with the tested processes and we don't expect there to be any deep cloning
         * ("au contraire") of the test data we can't compare the data sent in with data out and expect to detect
         * changes (these are in practice the same object). To do this we have to clone any data of interest before test
         *
         * @see #preProcessAll()
         */
        private void preProcess() {
            preProcessMostRecentContent = et.getEngagement().getMostRecentContent();
        }

        TestDataEnums(EngagementTransactionType et, String name) {
            this.et = et;
            logicalId = Hash.generateHashId(EntityTransformer.toEntity(et.getEngagement()));
            this.name = name;
        }

        /**
         * By contract the update method of ProcessBean updates the owner of the engagement to its "own owner" (this in
         * practice changes the key of the engagement making it unrecognizable). To deal with this for test purposes
         * we set the owner to the same logical entity as the "systems" owner before testing this method.
         * (and regenerate the internal key used for checking that any process dont break integrity of the engagement)
         *
         * @param owner the system owning the engagement
         * @see se.skltp.ei.svc.service.impl.ProcessBean#ProcessBean#update(Header header, UpdateType request)
         */
        void reset(String owner) {

            et =  GenServiceTestDataUtil.genEngagementTransaction(new Long(et.getEngagement().getRegisteredResidentIdentification()));
            et.getEngagement().setOwner(owner);
            et.setDeleteFlag(false);
            logicalId = Hash.generateHashId(EntityTransformer.toEntity(et.getEngagement()));
        }

        public EngagementTransactionType getEt() {
            return et;
        }

        /**
         * The Key is ree-generated from attributes of the "Incoming" engagement that makes this engagement to a
         * unique Engagement that can be bound to a certain care-receiver/care-giver having one or more engagements
         * <p>
         * For natural reasons any process receiving this data must not change that data. By Comparing this new key
         * with the same key generated at creation we ensure this doesn't happen
         *
         * @return natural-key/logical key
         * @see #TestDataEnums(EngagementTransactionType et, String name)
         * @see #reset(String owner)
         */
        public String getLogicalId() {
            String res = Hash.generateHashId(EntityTransformer.toEntity(et.getEngagement()));
            //Basically We don't want any one tamper with data that changes the logical Id
            assertEquals(res, logicalId);
            return res;
        }


        @Override
        public String toString() {
            return "TestDataEnums{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    static void resetEnumsEngagement(String owner) {
        for (TestDataEnums eTen : TestDataEnums.values()) {
            eTen.reset(owner);
        }
    }

    static void resetEnumsEngagement() {
        for (TestDataEnums eTen : TestDataEnums.values()) {
            eTen.reset("owner-of-" + eTen.name);
        }
    }

    static void preProcessAll() {
        for (TestDataEnums eTen : TestDataEnums.values()) {
            eTen.preProcess();
        }
    }

    static List<String> getEnumLogicalIds() {
        Set<String> res = new HashSet<>(TestDataEnums.values().length);
        for (TestDataEnums eTen : TestDataEnums.values()) {
            res.add(eTen.getLogicalId());
        }
        assertEquals(res.size(), TestDataEnums.values().length);
        return new ArrayList(res);
    }

}
