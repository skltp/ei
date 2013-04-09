package se.skltp.ei.svc.service;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import se.skltp.ei.svc.service.impl.FindContentBean;

public class FindContentBeanTest {

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
    public void r1_findContent_positive() throws Exception {
    	FindContentBean b = new FindContentBean();

        FindContentResponseType r = b.findContent(null, null);
        assertEquals(0, r.getEngagement().size());
    }

}
