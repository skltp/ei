package se.skltp.ei.subscriber;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.skltp.ei.subscriber.util.SubscriberFileTool;
import se.skltp.ei.util.FilterCreator;

import static org.junit.jupiter.api.Assertions.*;

public class SubscriberFileToolTest {

    @TempDir
    File tempDir;

    @Test
    public void saveAndRestoreSubscribersTest() {

        List<Subscriber> subscribers = FilterCreator.createOneSubscriber("HSA_ID_A", null);

        String filePath = tempDir.toString() + "/ei_cache.xml";
        SubscriberFileTool.saveToLocalCopy(subscribers, filePath);

        List<Subscriber> restoredSubscribers = SubscriberFileTool.restoreFromLocalCopy(filePath);
        assertEquals(1, restoredSubscribers.size());
        assertEquals("HSA_ID_A", restoredSubscribers.get(0).getLogicalAdress());
    }

}
