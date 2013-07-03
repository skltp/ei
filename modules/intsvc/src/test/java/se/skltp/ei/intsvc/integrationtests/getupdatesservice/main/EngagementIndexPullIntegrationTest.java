package se.skltp.ei.intsvc.integrationtests.getupdatesservice.main;

import org.junit.Assert;
import org.junit.Test;
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;

public class EngagementIndexPullIntegrationTest extends AbstractTestCase {

    public EngagementIndexPullIntegrationTest() {
        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

	@Override
	protected String getConfigResources() {
		return "get-updates/teststubs-and-services-config.xml";
	}

	@Test
	public void testSomething() {
		Assert.assertTrue(true);
	}

}
