package se.skltp.ei.svc.entity.util;

import java.util.HashSet;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import se.skltp.ei.svc.entity.model.util.MurmurHash;

public class MurmurHashTest {
	public static String prefix = "TestingHashWithAPrettyLongPrefixKeyJustAddedByASimpleNumberAndAlsoSomeInternationalCharactersSuchAsåäöÅÄÖ";

	@Test
	public void hashTest() {
		final int num = 10000;
		HashSet<Long> set = new HashSet<Long>(num);
		for (int i = 0; i < num; i++) {
			long hash1 = MurmurHash.hash64(prefix, String.valueOf(i));
			long hash2 = MurmurHash.hash64(prefix + i);
			assertEquals(hash1, hash2);
			set.add(hash1);
		}
		assertEquals(num, set.size());
	}
}
