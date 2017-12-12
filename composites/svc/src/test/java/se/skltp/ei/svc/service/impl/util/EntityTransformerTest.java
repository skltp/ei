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

        String fDate = EntityTransformer.formatDate(date);

        assertEquals(sDate, fDate);
    }

    @Test
    public void incorrectDateParseTest() {
        exception.expect(IllegalArgumentException.class);
        final String sDate = "1961-10-28 13:01:23";
        EntityTransformer.parseDate(sDate);
    }
}
