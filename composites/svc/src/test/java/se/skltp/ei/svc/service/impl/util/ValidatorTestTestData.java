package se.skltp.ei.svc.service.impl.util;

import riv.itintegration.engagementindex._1.EngagementTransactionType;

public class ValidatorTestTestData {
    public enum ValidationTestDataEnums {

        INVALID_CREATION_DATE(GenServiceTestDataUtil.genEngagementTransaction(1111111111L), new SetupETT() {
            @Override
            public void init(EngagementTransactionType et) {
                et.getEngagement().setCreationTime("2004-05-05");
            }
        }),
        INVALID_SOURCE_SYSTEM_LENGT(GenServiceTestDataUtil.genEngagementTransaction(1111111111L), new SetupETT() {
            @Override
            public void init(EngagementTransactionType et) {
                et.getEngagement().setSourceSystem("--------10--------20--------30--------40--------50--------60--------70");
            }

        }),
        INVALID_SOURCE_SYSTEM_WHITE_SPACE(GenServiceTestDataUtil.genEngagementTransaction(1111111111L), new SetupETT() {
            @Override
            public void init(EngagementTransactionType et) {
                et.getEngagement().setSourceSystem(" pre_and_post_white_space ");
            }


        }),
        INVALID_SOURCE_SYSTEM_MANDATORY(GenServiceTestDataUtil.genEngagementTransaction(1111111111L), new SetupETT() {
            @Override
            public void init(EngagementTransactionType et) {
                et.getEngagement().setSourceSystem(null);
            }


        });

        private EngagementTransactionType et;

        ValidationTestDataEnums(EngagementTransactionType et, SetupETT setup) {
            this.et = et;
            setup.init(et);
        }


        public EngagementTransactionType getEngagement() {
            return et;
        }
    }

}
