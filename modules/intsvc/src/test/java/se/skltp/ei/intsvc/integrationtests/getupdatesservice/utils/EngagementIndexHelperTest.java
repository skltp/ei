package se.skltp.ei.intsvc.integrationtests.getupdatesservice.utils;


import static junit.framework.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import se.skltp.ei.intsvc.getupdates.utils.EngagementIndexHelper;

@RunWith(MockitoJUnitRunner.class)
public class EngagementIndexHelperTest {

    @Test
    public void testStringToListThree() {
        List<String> stringList = EngagementIndexHelper.stringToList("a, b, c");
        assertEquals("Returning list should contain three elements!", 3, stringList.size());
        assertEquals("Returning list element 0 does not match the expected value!", "a", stringList.get(0));
        assertEquals("Returning list element 1 does not match the expected value!", "b", stringList.get(1));
        assertEquals("Returning list element 2 does not match the expected value!", "c", stringList.get(2));
    }

    @Test
    public void testStringToListOne() {
        List<String> stringList = EngagementIndexHelper.stringToList("abc");
        assertEquals("Returning list should contain one element!", 1, stringList.size());
        assertEquals("Returning list content doesn't match expected value!", "abc", stringList.get(0));
    }

    @Test
    public void testEmptyStringToList() {
        List<String> stringList = EngagementIndexHelper.stringToList("");
        assertEquals("Returning list is not null!", null, stringList);
    }

    @Test
    public void testNullStringToList() {
        List<String> stringList = EngagementIndexHelper.stringToList(null);
        assertEquals("Returning list is not null!", null, stringList);
    }

    @Test
    public void testDateOffsetBefore() throws ParseException {
        // Setup
        String insertedDateString = "20120515150000";
        String shouldReturnString = "20120511115759";
        String dateFormat = "yyyyMMddHHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Date insertedDate = simpleDateFormat.parse(insertedDateString);
        String dateOffset = "356521";
        // Test
        String actualReturnedString = EngagementIndexHelper.getFormattedOffsetTime(insertedDate, dateOffset, dateFormat);
        // Verify
        assertEquals("Returning timestamp does not match the expected one!", shouldReturnString, actualReturnedString);
    }

    @Test
    public void testDateOffsetSame() throws ParseException {
        // Setup
        String insertedDateString = "20120515150000";
        String shouldReturnString = "20120515150000";
        String dateFormat = "yyyyMMddHHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Date insertedDate = simpleDateFormat.parse(insertedDateString);
        String dateOffset = "0";
        // Test
        String actualReturnedString = EngagementIndexHelper.getFormattedOffsetTime(insertedDate, dateOffset, dateFormat);
        // Verify
        assertEquals("Returning timestamp does not match the expected one!", shouldReturnString, actualReturnedString);
    }

    @Test
    public void testDateOffsetAfter() throws ParseException {
        // Setup
        String insertedDateString = "20120515150000";
        String shouldReturnString = "20120519180201";
        String dateFormat = "yyyyMMddHHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Date insertedDate = simpleDateFormat.parse(insertedDateString);
        String dateOffset = "-356521";
        // Test
        String actualReturnedString = EngagementIndexHelper.getFormattedOffsetTime(insertedDate, dateOffset, dateFormat);
        // Verify
        assertEquals("Returning timestamp does not match the expected one!", shouldReturnString, actualReturnedString);
    }

}
