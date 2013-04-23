package se.skltp.ei.svc.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.impl.ProcessBean;

public class ProcessBeanTest {

    private static ProcessBean BEAN = null; 

    @BeforeClass
    public static void setUpClass() throws Exception {
        BEAN = new ProcessBean();
        EngagementRepository er = mock(EngagementRepository.class);
        BEAN.setEngagementRepository(er);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * R1 test with n engagements
     */
    @Test
    public void labb_r1_update_positive() throws Exception {

        UpdateType request = new UpdateType();
        UpdateResponseType r = BEAN.update(null, request );
        assertEquals(ResultCodeEnum.OK, r.getResultCode());
    }

    /**
     * test R1 for Update service with an positive test, i.e. two different engagements
     */
    @Test
    public void update_r1_positive() throws Exception {

        UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        EngagementTransactionType et2 = GenServiceTestDataUtil.genEngagementTransaction(2222222222L);

		request.getEngagementTransaction().add(et1);
		request.getEngagementTransaction().add(et2);
        
        UpdateResponseType r = BEAN.update(null, request);
        assertEquals(ResultCodeEnum.OK, r.getResultCode());
    }

    /**
     * test R1 for Update service with an negative test, i.e. null engagements (that is not allowed)
     */
    @Test
    public void update_r1_negative_null() throws Exception {

        try {
			UpdateType request = new UpdateType();
			EngagementTransactionType e1 = null;
			request.getEngagementTransaction().add(e1);
			
			BEAN.update(null, request);
			fail("Expected exception here");
		
        } catch (NullPointerException e) {
			// That was the expected exception, carry on...
		}
    }

    /**
     * test R1 for Update service with an negative test, i.e. two equal engagements (that is not allowed)
     */
    @Test
    public void update_r1_negative_equal() throws Exception {

        try {
			UpdateType request = new UpdateType();
			EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
			EngagementTransactionType et2 = GenServiceTestDataUtil.genEngagementTransaction(2222222222L);
		    EngagementTransactionType et3 = GenServiceTestDataUtil.genEngagementTransaction(3333333333L);

		    request.getEngagementTransaction().add(et3);
			request.getEngagementTransaction().add(et1);
			request.getEngagementTransaction().add(et2);
			request.getEngagementTransaction().add(et1);
			
			BEAN.update(null, request);
			fail("Expected exception here");

        } catch (RuntimeException e) {
			// That was the expected exception, carry on...
        	assertEquals("IE002, EngagementTransaction #2 and #4 have the same key. That is not allowed. See Upade R1 in service contract", e.getMessage());
		}
    }


}
