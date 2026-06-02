/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.entity.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GenEntityTestDataUtil {
	
    /**
     * Generates test data.
      */
    public static List<Engagement> generateEngagements(int startId, int size) {
        List<Engagement> list = new ArrayList<Engagement>();
        for (long i = 0; i < size; i++) {
            Engagement e = generateEngagement(startId + i);
            Date now = new Date();
            e.setMostRecentContent(now);
            list.add(e);
        }
        return list;
    }

    /**
     * Generates a key, which is completely derived from the value of residentIdentification (repeatable).
    */
    public static Engagement generateEngagement(long residentIdentification) {
        final String[] domains = { "urn:riv:scheduling:timebooking", "urn:riv:clinicalprocess:dummy", "urn:riv:another:test:doamin", "urn:riv:yet:another:dummy:domain" };
        final String[] categories = { "booking", "dummy", "one.two.three", "andsoforth" };
        final String[] logicalAdresses = { "SE100200400-600", "SE100200400-700", "SE100200400-800", "SE100200400-900" };
        final String[] sourceSystems = { "XXX100200400-600", "XXX100200400-700", "XXX100200400-800", "XXX100200400-900" };

        int n = (int)(residentIdentification % 4L);
        Engagement e = new Engagement();
        e.setBusinessKey("19" + residentIdentification,
                domains[n],
                categories[n],
                String.valueOf(residentIdentification),
                logicalAdresses[n],
                sourceSystems[n],
                "dataController",
                "Inera",
                "NA");

        return e;    	
    }

}
