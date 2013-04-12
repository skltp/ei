package se.skltp.ei.svc.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:skltp-ei-svc-spring-context.xml")
public class Benchmark {
	
	@Autowired
	private EngagementRepository engagementRepository;

	@Test
	public void insertTest() {
		Date d = new Date();
		List<Engagement> list = new ArrayList<Engagement>();
		for (long i = 0; i < 1000; i++) {
			Engagement e = new Engagement();
			e.setKey(genKey(i));
			e.setMostRecentContent(d);
			list.add(e);
			if ((i % 100) == 0) {
				engagementRepository.save(list);
				list.clear();
			}
		}
		engagementRepository.save(list);

		for (Engagement e : engagementRepository.findAll()) {
			System.out.println(e.getKey().getRegisteredResidentIdentification() + ", " + e.getCreationTime() + ", " + e.getMostRecentContent());
		}
	}
	   
    //
    static Engagement.Key genKey(long residentIdentification) {	
    	Engagement.Key key = Engagement.createKey();
    	key.setRegisteredResidentIdentification(String.valueOf("19" + residentIdentification));
    	key.setServiceDomain("urn:riv:scheduling:timebooking");
    	key.setBusinessObjectInstanceIdentifier(String.valueOf(residentIdentification));
    	key.setCategorization("booking");
    	key.setLogicalAddress("SE100200400-600");
    	key.setSourceSystem("SE300200-300");
    	return key;    	
    }


	/**
	 * Main.
	 * 
	 * @param args node.
	 */
	public static void main(String[] args) {

	}

}
