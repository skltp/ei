/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.entity.model.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import org.junit.jupiter.api.Test;

class HashTest {
  public static String prefix = "TestingHashWithAPrettyLongPrefixKeyJustAddedByASimpleNumberAndAlsoSomeInternationalCharactersSuchAsåäöÅÄÖ";


  @Test
  public void sha() {
    final int num = 1000;
    HashSet<String> set = new HashSet(num);
    for (int i = 0; i < num; i++) {
      String hash1 = Hash.sha2(prefix, String.valueOf(i));
      String hash2 = Hash.sha2(prefix + i);
      assertEquals(hash1, hash2);
      set.add(hash1);
    }
    assertEquals(num, set.size());
  }
}