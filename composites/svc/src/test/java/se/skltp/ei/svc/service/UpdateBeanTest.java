package se.skltp.ei.svc.service;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import se.skltp.ei.svc.service.impl.UpdateBean;

public class UpdateBeanTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
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
        UpdateBean u = new UpdateBean();

        UpdateResponseType r = u.update(null, null);
        assertEquals(ResultCodeEnum.OK, r.getResultCode());
    }

}
