package se.skltp.ei.svc.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:skltp-ei-mysql-benchmark-test.xml")
public class BenchmarkTest {
	
	static class Timer {
		private long t0 = System.currentTimeMillis();
		private int elapsed;
		private String name;
		private int rows;
		private static List<Timer> timers = new LinkedList<Timer>();

		Timer(String name) {
			this.name = name;
			timers.add(this);
		}
		
		void stop(int rows) {
			this.elapsed = (int)(System.currentTimeMillis() - t0);
			this.rows = rows;
		}
		
		int elapsed() {
			return elapsed;
		}
		
		String name() {
			return name;
		}
		
		int rows() {
			return rows;
		}
		
		float perf() {
			return ((rows() * 1000.0f) / elapsed());
		}
		
		static List<Timer> getTimers() {
			return timers;
		}
		
		@Override
		public String toString() {
			return String.format("%s: { rows: %d, elapsed: %d, rows/s: %.2f }", name(), rows(), elapsed(), perf());
		}
	}
	
	
	Logger logger = Logger.getLogger(BenchmarkTest.class);
		
	@Autowired
	private EngagementRepository engagementRepository;

	@Value("${benchmarkRows}")
	private int rows;

	@Value("${benchmarkBatchSize}")
	private int batchSize;
	
	
	@Transactional
	public int upsertBatch(int size) {
		engagementRepository.save(genEngagements(size));
		return size;
	}

	public int saveTest(String name) {
		int total = 0;
		Timer t = new Timer(name);
		for (; total < rows;) {
			Timer tb = new Timer(name + "Batch");
			total += upsertBatch(batchSize);
			tb.stop(batchSize);
		}
		t.stop(total);
		return total;
	}


	@Test
	public void benchmark() {
		logger.info("benchmark rows: " + rows);
		
		int total = 0;
		
		Timer t = new Timer("Total");
		
		total += saveTest("Insert");
		
		total += saveTest("Update");
		
		t.stop(total);
		
		for (Timer timer : Timer.getTimers()) {
			logger.info(timer);
		}
		
	}
	
	
	static List<Engagement> genEngagements(int n) {
		List<Engagement> list = new ArrayList<Engagement>();
		for (long i = 0; i < n; i++) {
			Engagement e = new Engagement();
			e.setKey(genKey(i));
			Date now = new Date();
			e.setMostRecentContent(now);
			e.setCreationTime(now);
			list.add(e);
		}
		return list;
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
