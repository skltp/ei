/*
  Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>

  This file is part of SKLTP.

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.svc.entity;

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
@ContextConfiguration(locations = {"classpath:skltp-ei-svc-spring-context.xml", "classpath:skltp-ei-svc-test-spring-context.xml"})
public class BenchmarkTest {

    static class Timer {
        private long t0 = System.currentTimeMillis();
        private int elapsed;
        private String name;
        private int rows;
        private static List<Timer> timers = new LinkedList<>();

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


    private static Logger logger = Logger.getLogger(BenchmarkTest.class);

    @Autowired
    private EngagementRepository engagementRepository;

    @Value("${benchmarkRows:10}")
    private int rows;

    @Value("${benchmarkBatchSize:5}")
    private int batchSize;


    /**
     * The transactional batch update part.
     * 
     * @param start the start number.
     * @param size the batch size.
     * @return size
     */
    @Transactional
    public int upsertBatch(int start, int size) {
        engagementRepository.save(GenEntityTestDataUtil.genEngagements(start, size));
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
}
