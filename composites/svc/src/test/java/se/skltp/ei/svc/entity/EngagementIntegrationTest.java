package se.skltp.ei.svc.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:skltp-ei-svc-spring-context.xml")
public class EngagementIntegrationTest {

    @Autowired
    private EngagementRepository engagementRepository;

    @Test
    public void shouldFindPreviouslySavedPerson() {

        String businessObjectInstanceIdentifier = "boi";
        String categorization = "categorization";
        String logicalAddress = "logicalAddress";

    	// given
        Engagement engagement = new Engagement();
        engagement.setBusinessObjectInstanceIdentifier(businessObjectInstanceIdentifier);
		engagement.setCategorization(categorization);
		engagement.setLogicalAddress(logicalAddress);
        engagementRepository.save(engagement);

        // when
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();

        // then
        assertThat(result, hasSize(1));

        Engagement foundEngagement = result.get(0);
        assertThat(foundEngagement.getBusinessObjectInstanceIdentifier(), is(businessObjectInstanceIdentifier));
        assertThat(foundEngagement.getCategorization(), is(categorization));
        assertThat(foundEngagement.getLogicalAddress(), is(logicalAddress));
    }
}
