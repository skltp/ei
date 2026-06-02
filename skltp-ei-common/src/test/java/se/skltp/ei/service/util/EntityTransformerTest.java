/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.service.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EntityTransformerTest {

  @Test
  public void dateParseTest() {
    final String sDate = "19611028130123";

    Date date = EntityTransformer.parseDate(sDate);
    String fDate = EntityTransformer.formatDate(date);

    assertEquals(sDate, fDate);
  }

  @Test
  public void incorrectDateParseTest() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      EntityTransformer.parseDate("1961-10-28 13:01:23");
    });
  }
}