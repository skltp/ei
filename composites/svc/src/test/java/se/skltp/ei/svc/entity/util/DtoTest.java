package se.skltp.ei.svc.entity.util;
import org.junit.Test;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.GenEntityTestDataUtil;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.service.impl.util.GenServiceTestDataUtil;
import se.skltp.ei.svc.service.impl.util.SortedEngagementsData;

import static org.junit.Assert.*;

public class DtoTest {



    @Test
    public void testIncomingEngagementProcessData(){

        UpdateType request = new UpdateType();

        SortedEngagementsData data = new SortedEngagementsData();


        assertNotNull(data.engagementsToDelete());


        assertFalse(data.engagementsToDelete().iterator().hasNext());

        assertNotNull(data.engagementsToSave());

        assertFalse(data.engagementsToSave().iterator().hasNext());

        assertFalse(data.existsAnythingToSave());

        assertFalse(data.existsAnythingToDelete());





        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L,"MAS");
        Engagement e1 = GenEntityTestDataUtil.genEngagement(1111111111L,"Inera");
        et1.setDeleteFlag(true);

        EngagementTransactionType et2 = GenServiceTestDataUtil.genEngagementTransaction(1111111112L,"SÖS");
        Engagement e2 = GenEntityTestDataUtil.genEngagement(1111111112L,"Inera");
        et2.setDeleteFlag(false);

        request.getEngagementTransaction().add(et1);
        request.getEngagementTransaction().add(et2);

        data = new SortedEngagementsData();



        int i=0;
        for(EngagementTransactionType engagementTransactionType:request.getEngagementTransaction()){
            i++;

            if(engagementTransactionType.isDeleteFlag())
                data.addForDeletion(e1,engagementTransactionType);
            else
                data.addForSaving(e2,engagementTransactionType);

            data.addForDeletion(GenEntityTestDataUtil.genEngagement(1111111112L+i,"test"+i),null);

        }

        assertTrue(data.existsAnythingToSave());

        assertTrue(data.existsAnythingToDelete());



        assertTrue(data.existsAnythingToDelete());

        assertTrue(data.engagementsToSave().iterator().hasNext());





    }



}
