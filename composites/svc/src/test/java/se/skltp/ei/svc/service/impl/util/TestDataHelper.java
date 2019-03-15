package se.skltp.ei.svc.service.impl.util;


import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.model.util.Hash;
import se.skltp.ei.svc.service.api.Header;
import static junit.framework.Assert.assertEquals;

public class TestDataHelper {
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
    public enum TestDataEnums {
        MR_PINK_1(GenServiceTestDataUtil.genEngagementTransaction(1111111111L), "Mr-Pink(1)"),
        MR_BROWN_2(GenServiceTestDataUtil.genEngagementTransaction(2222222222L), "Mr-Brown(2)"),
        MS_SALLY_3(GenServiceTestDataUtil.genEngagementTransaction(3333333333L), "Ms-Sally(3)"),
        MR_BEAN_4(GenServiceTestDataUtil.genEngagementTransaction(4444444444L), "Mr-Bean(4)"),
        MR_BLACK_5(GenServiceTestDataUtil.genEngagementTransaction(5555555555L), "Mr-Black(5)");

        private EngagementTransactionType et;

        private String logicalId;

        private String name;

        /**
         * @return MostRecentContent value before process
         * @see #preProcess()
         * se.skltp.ei.svc.service.impl.util.ProcessBeanIntegrationTestHelper#updateOrProcessNotification(ProcessBean, Object)
         */
        public String getPreProcessMostRecentContent() {
            return preProcessMostRecentContent;
        }

        private String preProcessMostRecentContent;

        public String getPreResetLogicalId() {
            return preResetLogicalId;
        }

        private String preResetLogicalId;

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
            preResetLogicalId = null;
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
        public void reset(String owner) {

/*
            if(et==null){
                et = GenServiceTestDataUtil.genEngagementTransaction(new Long(et.getEngagement().getRegisteredResidentIdentification()),owner);
            }
*/
            et.getEngagement().setMostRecentContent(null);
            et.getEngagement().setUpdateTime(null);
            et.getEngagement().setCreationTime(null);
            et.getEngagement().setOwner(owner);
            et.setDeleteFlag(false);
            preResetLogicalId = logicalId;
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

    public static void resetEnumsEngagement(String owner) {
        for (TestDataEnums eTen : TestDataEnums.values()) {
            eTen.reset(owner);
        }
    }

    public static void resetEnumsEngagement() {
        for (TestDataEnums eTen : TestDataEnums.values()) {
            eTen.reset("owner-of-" + eTen.name);
        }
    }

    static void preProcessAll() {
        for (TestDataEnums eTen : TestDataEnums.values()) {
            eTen.preProcess();
        }
    }



}
