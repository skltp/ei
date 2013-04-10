package se.skltp.ei.svc.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.impl.FindContentBean;

public class FindContentBeanTest {

	private static FindContentBean BEAN = null; 
	
	@BeforeClass
    public static void setUpClass() throws Exception {
    	BEAN = new FindContentBean();
    	EngagementRepository er = mock(EngagementRepository.class);
		BEAN.setEngagementRepository(er);
    	
		when(er.findAll()).thenAnswer(new Answer<Iterable<Engagement>>() {
		    @Override
		    public Iterable<Engagement> answer(InvocationOnMock invocation) throws Throwable {
		        List<Engagement> list = new ArrayList<Engagement>(); 
		    	return list;
		    }
		});		
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
    public void r1_findContent_ok() throws Exception {
    	
        FindContentResponseType r = BEAN.findContent(null, null);
        assertEquals(0, r.getEngagement().size());
    }

}
