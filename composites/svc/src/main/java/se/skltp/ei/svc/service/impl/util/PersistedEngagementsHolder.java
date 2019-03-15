package se.skltp.ei.svc.service.impl.util;

import se.skltp.ei.svc.entity.model.Engagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PersistedEngagementsHolder {
    private Map<String, Engagement> persistedEngagementsCorrespondingToIncoming;

    private List<Engagement> persistedThatNoLongerBelongsToUs;
    /**
     * To avoid two db lookups we want to fetch all persisted engagements at once but keep em separated in the result
     * since they are used by different methods for different purposes
     */
    public PersistedEngagementsHolder(List<Engagement> allFetchedEngagements,List<String> idOfEngagementsNoLongerBelongingToUs){
        persistedEngagementsCorrespondingToIncoming = new HashMap<>();
        persistedThatNoLongerBelongsToUs = new ArrayList<>();
        for(Engagement engagement:allFetchedEngagements){
            if(idOfEngagementsNoLongerBelongingToUs!=null&&idOfEngagementsNoLongerBelongingToUs.contains(engagement.getId())){
                persistedThatNoLongerBelongsToUs.add(engagement);
            }else{
                persistedEngagementsCorrespondingToIncoming.put(engagement.getId(),engagement);
            }

        }
    }

    /**
     * persisted Engagements with a logical key that is equal to a incoming engagement.
     * @return
     */
    public Map<String, Engagement> getPersistedEngagementsCorrespondingToIncoming() {
        return persistedEngagementsCorrespondingToIncoming;
    }

    /**
     * Persisted Engagements corresponding to a incoming engagement where the owner is no longer this instance owner.
     *
     * @return
     */
    public List<Engagement> getPersistedThatNoLongerBelongsToUs() {
        return persistedThatNoLongerBelongsToUs;
    }


}
