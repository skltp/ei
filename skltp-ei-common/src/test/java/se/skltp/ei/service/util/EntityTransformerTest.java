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