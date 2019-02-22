package se.skltp.ei.svc.entity.util;
import org.junit.Test;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.GenEntityTestDataUtil;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.service.GenServiceTestDataUtil;
import se.skltp.ei.svc.service.impl.util.IncomingEngagementProcessData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class DtoTest {



    @Test
    public void testIncomingEngagementProcessData(){

        UpdateType request = new UpdateType();

        IncomingEngagementProcessData data = IncomingEngagementProcessData.createForUpdate(request.getEngagementTransaction(),"Inera");

        assertEquals(0, data.size());

        assertNotNull(data.engagementsToDelete());


        assertFalse(data.engagementsToDelete().iterator().hasNext());

        assertNotNull(data.engagementsToSave());

        assertFalse(data.engagementsToSave().iterator().hasNext());

        assertFalse(data.existsAnythingToSave());

        assertFalse(data.existsAnythingToDelete());

        assertNotNull(data.getProcessResult());

        assertFalse(data.getProcessResult().iterator().hasNext());




        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L,"MAS");
        Engagement e1 = GenEntityTestDataUtil.genEngagement(1111111111L,"Inera");
        et1.setDeleteFlag(true);

        EngagementTransactionType et2 = GenServiceTestDataUtil.genEngagementTransaction(1111111112L,"SÖS");
        Engagement e2 = GenEntityTestDataUtil.genEngagement(1111111112L,"Inera");
        et2.setDeleteFlag(false);

        request.getEngagementTransaction().add(et1);
        request.getEngagementTransaction().add(et2);

        data = IncomingEngagementProcessData.createForUpdate(request.getEngagementTransaction(),"Inera");

        assertEquals(2, data.size());


        int i=0;
        for(EngagementTransactionType engagementTransactionType:data){
            i++;
            assertEquals("Inera", engagementTransactionType.getEngagement().getOwner());
            if(engagementTransactionType.isDeleteFlag())
                data.addForDeletion(e1,engagementTransactionType);
            else
                data.addForSaving(e2,engagementTransactionType);

            data.addForDeletion(GenEntityTestDataUtil.genEngagement(1111111112L+i,"test"+i),null);

        }
        assertEquals(2, data.size());

        assertTrue(data.existsAnythingToSave());

        assertTrue(data.existsAnythingToDelete());

        List<String> ids =  data.getSaveCandidateIds();

        assertEquals(e2.getId(), ids.iterator().next());

        data.markAsRemoveFromSaveList(e2);

        assertFalse(data.existsAnythingToSave());

        assertTrue(data.existsAnythingToDelete());

        assertTrue(data.engagementsToSave().iterator().hasNext());

        assertFalse(data.engagementsToSave(true).iterator().hasNext());

        Map<String,Engagement> persitedMap = new HashMap<>();

        data.setPersistedEngagementMap(persitedMap);

        assertNull("",data.getPersistedEngagement(e2.getId()));

        persitedMap.put(e2.getId(),e2);

        persitedMap.put(e1.getId(),e1);

        assertEquals(data.getPersistedEngagement(e2.getId()), e2);

        assertEquals(2, data.size());

    }

    @Test(expected = IllegalStateException.class)
    public void testGetPersistedEngagementUnassigned(){
        IncomingEngagementProcessData data = IncomingEngagementProcessData.createForUpdate(null,"Inera");
        data.getPersistedEngagement("hej");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPersistedEngagementUnassigned(){
        IncomingEngagementProcessData data = IncomingEngagementProcessData.createForUpdate(null,"Inera");
        data.setPersistedEngagementMap(null);
    }
}
