
package se.skltp.ei.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import se.skltp.ei.entity.model.Engagement;

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
