package se.skltp.ei.svc.entity.util;

import java.util.HashSet;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import se.skltp.ei.svc.entity.model.util.Hash;

/**
 * Tests the hash algorithm.
 * 
 * @author Peter
 */
public class HashTest {
	public static String prefix = "TestingHashWithAPrettyLongPrefixKeyJustAddedByASimpleNumberAndAlsoSomeInternationalCharactersSuchAsåäöÅÄÖ";

	
	@Test
	public void sha() {
		final int num = 1000;
		HashSet<String> set = new HashSet<String>(num);
		for (int i = 0; i < num; i++) {
			String hash1 = Hash.sha2(prefix, String.valueOf(i));
			String hash2 = Hash.sha2(prefix + i);
			assertEquals(hash1, hash2);
			set.add(hash1);
		}
		assertEquals(num, set.size());		
	}
}
