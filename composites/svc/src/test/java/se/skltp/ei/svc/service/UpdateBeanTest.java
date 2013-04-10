package se.skltp.ei.svc.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.impl.FindContentBean;
import se.skltp.ei.svc.service.impl.UpdateBean;

public class UpdateBeanTest {

	private static UpdateBean BEAN = null; 
	
	@BeforeClass
    public static void setUpClass() throws Exception {
    	BEAN = new UpdateBean();
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
    public void r1_update_positive() throws Exception {

        UpdateType request = new UpdateType();
		UpdateResponseType r = BEAN.update(null, request );
        assertEquals(ResultCodeEnum.OK, r.getResultCode());
    }

}
