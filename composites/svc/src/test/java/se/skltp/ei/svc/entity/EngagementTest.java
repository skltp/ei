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
package se.skltp.ei.svc.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import se.skltp.ei.svc.entity.model.BusinessKey;
import se.skltp.ei.svc.entity.model.Engagement;

/**
 * Unit test for Engagement.
 * 
 * @author Peter
 */
public class EngagementTest {

    @Test
    public void businessKeyTest() {
        Engagement e1 = GenEntityTestDataUtil.genEngagement(1L);
        Engagement e2 = GenEntityTestDataUtil.genEngagement(1L);
        Engagement e3 = GenEntityTestDataUtil.genEngagement(2L);

        BusinessKey key1 = e1.getBusinessKey();
        BusinessKey key2 = e2.getBusinessKey();
        BusinessKey key3 = e3.getBusinessKey();

        assertEquals(e1.getId(), e2.getId());
        assertFalse(e1.getId().equals(e3.getId()));
        assertTrue(key1.equals(key2));
        assertEquals(key1.hashCode(), key2.hashCode());
        assertTrue(key1.hashCode() != key3.hashCode());
        assertFalse(key1.equals(key3));
    }

    @Test
    public void equalsTest() {
        Engagement e1 = GenEntityTestDataUtil.genEngagement(1L);
        Engagement e2 = GenEntityTestDataUtil.genEngagement(1L);
        Engagement e3 = GenEntityTestDataUtil.genEngagement(2L);
        Engagement e4 = new Engagement();
        Engagement e5 = new Engagement();
        assertTrue(e1.equals(e2));
        assertFalse(e1.equals(e3));
        assertFalse(e4.equals(e5));
        assertFalse(e4.equals(e1));
    }

    @Test
    public void hashCodeTest() {
        Engagement e1 = GenEntityTestDataUtil.genEngagement(1L);
        Engagement e2 = GenEntityTestDataUtil.genEngagement(1L);
        Engagement e3 = GenEntityTestDataUtil.genEngagement(2L);
        Engagement e4 = new Engagement();
        assertTrue(e1.hashCode() == e2.hashCode());
        assertFalse(e1.hashCode() == e3.hashCode());
        assertTrue(e4.hashCode() == 0);
        assertEquals(e4.getId(), null);
    }

}
