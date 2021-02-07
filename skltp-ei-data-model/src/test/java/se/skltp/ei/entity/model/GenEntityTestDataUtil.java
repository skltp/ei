/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
