package se.skltp.ei.svc.service;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.impl.ProcessBean;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:skltp-ei-svc-spring-context.xml")
public class ProcessBeanIntegrationTest {

    private static ProcessBean BEAN = null; 
    private static final String OWNER = "logical-address";

    @Autowired
    private EngagementRepository engagementRepository;

    @Before
    public void setUp() throws Exception {

        // Clean the storage
        engagementRepository.deleteAll();

        BEAN = new ProcessBean();
        BEAN.setEngagementRepository(engagementRepository);
        BEAN.setOwner(OWNER);

    }


    @Test
    public void update_r6_positive_owner_should_be_set_when_saved() {

        // Create a request
        UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);

        // Using a wrong owner to test that is overwritten with the correct one
        et1.getEngagement().setOwner("wrong-owner");
        request.getEngagementTransaction().add(et1);

        //Validate that the request went through
        UpdateResponseType r = BEAN.update(null, request);
        assertEquals(ResultCodeEnum.OK, r.getResultCode());

        // Fetch last post
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();

        // Should be only one post
        assertThat(result, hasSize(1));

        // Validate the owner is the correct one
        Engagement foundEngagement = result.get(0);
        assertThat(foundEngagement.getBusinessKey().getOwner(), equalTo(OWNER));
    }


}
