package se.skltp.ei.svc.entity;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:skltp-ei-svc-spring-context.xml")
public class EngagementIntegrationTest {

    @Autowired
    private EngagementRepository engagementRepository;

    @Before
    public void setUp() throws Exception {

    	// Clean the storage
    	engagementRepository.deleteAll();
    }
    
    @Test
    public void shouldFindPreviouslySavedPerson() {

        // given
        Engagement engagement  = GenEntityTestDataUtil.genEngagement(1212121212L);
        engagementRepository.save(engagement);

        // when
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();

        // then
        assertThat(result, hasSize(1));

        Engagement foundEngagement = result.get(0);
        assertThat(foundEngagement.getBusinessKey(),is(engagement.getBusinessKey()));
    }

    @Test
    public void findByMultipleKeys() {

    	// given
        final int num = 1000;
        List<Engagement> list = GenEntityTestDataUtil.genEngagements(0, num);
        engagementRepository.save(list);

        // when
        List<String> ids = new LinkedList<String>();
        for (Engagement e : list) {
            ids.add(e.getId());
        }
        List<Engagement> result = engagementRepository.findByIdIn(ids);

        // then
        assertEquals(num, result.size());
    }

    @Test
    public void findByRegisteredResidentIdentification() {

    	// given
    	final int num = 10;
        List<Engagement> list = GenEntityTestDataUtil.genEngagements(0, num);
        engagementRepository.save(list);

        // when
        Engagement e = GenEntityTestDataUtil.genEngagement(1);
        Engagement.BusinessKey key = e.getBusinessKey();
        List<Engagement> result = engagementRepository.findByBusinessKey_RegisteredResidentIdentification(key.getRegisteredResidentIdentification());

        // then
        assertEquals(1, result.size());
        assertThat(result.get(0).getBusinessKey(),is(key));
    }
}
