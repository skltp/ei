package se.skltp.ei.svc.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import se.skltp.ei.svc.entity.model.Engagement;

public class GenTestDataUtil {
	
    /**
     * Generates test data.
     * 
     * @param start start id.
     * @param size batch size.
     * @return list of test engagements.
     */
    public static List<Engagement> genEngagements(int start, int size) {
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
    public static Engagement genEngagement(long residentIdentification) {
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
                "Inera",
                "NA");

        e.setCreationTime(new Date());

        return e;    	
    }

}
