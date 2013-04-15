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

/**
 * Performs a simple benchmark inserting and updating EI records. <p>
 * 
 * The default configuration is just a few records, and this can be overrided in a
 * local properties file, see context configuration below.
 * 
 * @author Peter
 */
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
			logger.info(this);
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
	
	
	static Logger logger = Logger.getLogger(BenchmarkTest.class);
		
	@Autowired
	private EngagementRepository engagementRepository;

	@Value("${benchmarkRows}")
	private int rows;

	@Value("${benchmarkBatchSize}")
	private int batchSize;
	
	
	/**
	 * The transactional batch update part.
	 * 
	 * @param start the start number.
	 * @param size the batch size.
	 * @return
	 */
	@Transactional
	public int upsertBatch(int start, int size) {
		engagementRepository.save(genEngagements(start, size));
		return size;
	}

	/**
	 * Saves records.
	 * 
	 * @param name name of operation for sampling timing statistics.
	 * @return the number of records saved.
	 */
	public int saveTest(String name) {
		int total = 0;
		Timer t = new Timer(name);
		for (; total < rows;) {
			Timer tb = new Timer(name + "Batch");
			total += upsertBatch(total, batchSize);
			tb.stop(batchSize);
		}
		t.stop(total);
		return total;
	}


	/**
	 * The test method. <p>
	 * 
	 * First create records, and then update all of them.
	 */
	@Test
	public void benchmark() {
		logger.info("benchmark rows: " + rows);
		
		int total = 0;
		
		Timer t = new Timer("Total");
		
		total += saveTest("Insert");
		
		total += saveTest("Update");
		
		t.stop(total);		
	}
	
	
	/**
	 * Generates test data.
	 * 
	 * @param start start id.
	 * @param size batch size.
	 * @return list of test engagements.
	 */
	static List<Engagement> genEngagements(int start, int size) {
		List<Engagement> list = new ArrayList<Engagement>();
		for (long i = 0; i < size; i++) {
			Engagement e = genEngagement(start + i);
			Date now = new Date();
			e.setMostRecentContent(now);
			e.setCreationTime(now);
			list.add(e);
		}
		return list;
	}

	   
    /**
     * Generates a key, which is completely derived from the value of residentIdentification (repeatable).
     * 
     * @param e the engagement
     * @return the generated engagement with an updated key
     */
    static Engagement genEngagement(long residentIdentification) {
    	final String[] domains = { "urn:riv:scheduling:timebooking", "urn:riv:clinicalprocess:dummy", "urn:riv:another:test:doamin", "urn:riv:yet:another:dummy:domain" };
    	final String[] categories = { "booking", "dummy", "one.two.three", "andsoforth" };
    	final String[] logicalAdresses = { "SE100200400-600", "SE100200400-700", "SE100200400-800", "SE100200400-900" };
    	final String[] sourceSystems = { "XXX100200400-600", "XXX100200400-700", "XXX100200400-800", "XXX100200400-900" };
    	
       	int n = (int)(residentIdentification % 4L);
       	Engagement e = new Engagement();
       	e.setBusinessKey(String.valueOf("19" + residentIdentification),
			domains[n],
			categories[n],
			String.valueOf(residentIdentification),
			logicalAdresses[n],
			sourceSystems[n],
			"Inera",
			"NA");
       	
       	e.setCreationTime(new Date());
       	
       	return e;    	
    }
}
