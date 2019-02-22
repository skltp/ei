/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.svc.entity.util;

import java.util.HashSet;

import org.junit.Test;

import se.skltp.ei.svc.entity.GenEntityTestDataUtil;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.model.util.Hash;

import static javatests.TestSupport.assertNotEquals;
import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

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


    @Test
    public void generateHashId() {
        Engagement e1 = GenEntityTestDataUtil.genEngagement(1L);
        Engagement e2 = GenEntityTestDataUtil.genEngagement(1L);
        Engagement e3 = GenEntityTestDataUtil.genEngagement(1l,"Mr Brown");

        Engagement e4 = GenEntityTestDataUtil.genEngagement(2l,"Mr Brown");

        String h1 = Hash.generateHashId(e1);

        String h2 = Hash.generateHashId(e2);

        assertEquals(h1,h2);

        h2 = e1.getId();
        assertEquals(h1,h2);

        h2 = Hash.generateHashId(e2, "Mr Pink");

        assertNotEquals(h1,h2,"");

        h1 = Hash.generateHashId(e3);

        assertNotEquals(h1,h2,"");

        h2 = Hash.generateHashId(e2, "Mr Brown");

        assertEquals(h1,h2);

        h1 = Hash.generateHashId(e3);
        h2 = Hash.generateHashId(e4);

        assertNotEquals(h1,h2,"");
    }
}
