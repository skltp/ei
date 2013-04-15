package se.skltp.ei.svc.service.impl.util;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class EntityTransformerTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Test
	public void dateParseTest() {		
		final String sDate = "19611028130123";
		
		Date date = EntityTransformer.parseDate(sDate);
		
		String fDate = EntityTransformer.forrmatDate(date);
		
		assertEquals(sDate, fDate);
	}
	
	@Test
	public void incorrectDateParseTest() {
		exception.expect(IllegalArgumentException.class);
		final String sDate = "1961-10-28 13:01:23";
		EntityTransformer.parseDate(sDate);
	}
}
