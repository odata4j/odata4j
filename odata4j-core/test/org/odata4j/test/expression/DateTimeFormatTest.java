package org.odata4j.test.expression;

import junit.framework.Assert;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;
import org.odata4j.internal.InternalUtil;

public class DateTimeFormatTest {
	
	@Test
	public void testyyyyMMddHHmm() {
		DateTime dt = InternalUtil.parseDateTime("2010-12-20T17:34");
		
		Assert.assertEquals(2010, dt.getYear());
		Assert.assertEquals(12, dt.getMonthOfYear());
		Assert.assertEquals(20, dt.getDayOfMonth());
		Assert.assertEquals(17, dt.getHourOfDay());
		Assert.assertEquals(34, dt.getMinuteOfHour());
		Assert.assertEquals(0, dt.getSecondOfMinute());
		Assert.assertEquals(0, dt.getMillisOfSecond());
	}
	
	@Test
	public void testyyyyMMddHHmmss() {
		DateTime dt = InternalUtil.parseDateTime("2010-12-20T17:34:05");
		
		Assert.assertEquals(2010, dt.getYear());
		Assert.assertEquals(12, dt.getMonthOfYear());
		Assert.assertEquals(20, dt.getDayOfMonth());
		Assert.assertEquals(17, dt.getHourOfDay());
		Assert.assertEquals(34, dt.getMinuteOfHour());
		Assert.assertEquals(5, dt.getSecondOfMinute());
		Assert.assertEquals(0, dt.getMillisOfSecond());
	}

	@Test
	public void testyyyyMMddHHmmssfffffff() {
		DateTime dt = InternalUtil.parseDateTime("2010-12-20T17:34:05.1234567");
		
		Assert.assertEquals(2010, dt.getYear());
		Assert.assertEquals(12, dt.getMonthOfYear());
		Assert.assertEquals(20, dt.getDayOfMonth());
		Assert.assertEquals(17, dt.getHourOfDay());
		Assert.assertEquals(34, dt.getMinuteOfHour());
		Assert.assertEquals(5, dt.getSecondOfMinute());
		Assert.assertEquals(123, dt.getMillisOfSecond());
	}

	@Test
	public void testyyyyMMddHHmmssfffffffZZ() {
		DateTime dt = InternalUtil.parseDateTime("2010-12-20T17:34:05.1234567Z");
		dt = dt.toDateTime(DateTimeZone.UTC);
		
		Assert.assertEquals(2010, dt.getYear());
		Assert.assertEquals(12, dt.getMonthOfYear());
		Assert.assertEquals(20, dt.getDayOfMonth());
		Assert.assertEquals(17, dt.getHourOfDay());
		Assert.assertEquals(34, dt.getMinuteOfHour());
		Assert.assertEquals(5, dt.getSecondOfMinute());
		Assert.assertEquals(123, dt.getMillisOfSecond());
	}
	
	@Test
	public void testyyyyMMddHHmmssZZ() {
		DateTime dt = InternalUtil.parseDateTime("2010-12-20T17:34:05Z");
		dt = dt.toDateTime(DateTimeZone.UTC);
		
		Assert.assertEquals(2010, dt.getYear());
		Assert.assertEquals(12, dt.getMonthOfYear());
		Assert.assertEquals(20, dt.getDayOfMonth());
		Assert.assertEquals(17, dt.getHourOfDay());
		Assert.assertEquals(34, dt.getMinuteOfHour());
		Assert.assertEquals(5, dt.getSecondOfMinute());
		Assert.assertEquals(0, dt.getMillisOfSecond());
	}

	@Test
	public void testyyyyMMddHHmmZZ() {
		DateTime dt = InternalUtil.parseDateTime("2010-12-20T17:34Z");
		dt = dt.toDateTime(DateTimeZone.UTC);
		
		Assert.assertEquals(2010, dt.getYear());
		Assert.assertEquals(12, dt.getMonthOfYear());
		Assert.assertEquals(20, dt.getDayOfMonth());
		Assert.assertEquals(17, dt.getHourOfDay());
		Assert.assertEquals(34, dt.getMinuteOfHour());
		Assert.assertEquals(0, dt.getSecondOfMinute());
		Assert.assertEquals(0, dt.getMillisOfSecond());
	}
	
	@Test
	public void testyyyyMMddHHmmp0200() {
		DateTime dt = InternalUtil.parseDateTime("2010-12-20T17:34+02:00");
		dt = dt.toDateTime(DateTimeZone.UTC);
		
		Assert.assertEquals(2010, dt.getYear());
		Assert.assertEquals(12, dt.getMonthOfYear());
		Assert.assertEquals(20, dt.getDayOfMonth());
		Assert.assertEquals(15, dt.getHourOfDay());
		Assert.assertEquals(34, dt.getMinuteOfHour());
		Assert.assertEquals(0, dt.getSecondOfMinute());
		Assert.assertEquals(0, dt.getMillisOfSecond());
	}

	@Test
	public void testyyyyMMddHHmmm0600() {
		DateTime dt = InternalUtil.parseDateTime("2010-12-20T17:34-06:00");
		dt = dt.toDateTime(DateTimeZone.UTC);
		
		Assert.assertEquals(2010, dt.getYear());
		Assert.assertEquals(12, dt.getMonthOfYear());
		Assert.assertEquals(20, dt.getDayOfMonth());
		Assert.assertEquals(23, dt.getHourOfDay());
		Assert.assertEquals(34, dt.getMinuteOfHour());
		Assert.assertEquals(0, dt.getSecondOfMinute());
		Assert.assertEquals(0, dt.getMillisOfSecond());
	}
	
	@Test
	public void testyyyyMMddHHmmssm0600() {
		DateTime dt = InternalUtil.parseDateTime("2010-12-20T17:34:05-06:00");
		dt = dt.toDateTime(DateTimeZone.UTC);
		
		Assert.assertEquals(2010, dt.getYear());
		Assert.assertEquals(12, dt.getMonthOfYear());
		Assert.assertEquals(20, dt.getDayOfMonth());
		Assert.assertEquals(23, dt.getHourOfDay());
		Assert.assertEquals(34, dt.getMinuteOfHour());
		Assert.assertEquals(5, dt.getSecondOfMinute());
		Assert.assertEquals(0, dt.getMillisOfSecond());
	}

	@Test
	public void testyyyyMMddHHmmssfffffffm0600() {
		DateTime dt = InternalUtil.parseDateTime("2010-12-20T17:34:05.1234567-06:00");
		dt = dt.toDateTime(DateTimeZone.UTC);
		
		Assert.assertEquals(2010, dt.getYear());
		Assert.assertEquals(12, dt.getMonthOfYear());
		Assert.assertEquals(20, dt.getDayOfMonth());
		Assert.assertEquals(23, dt.getHourOfDay());
		Assert.assertEquals(34, dt.getMinuteOfHour());
		Assert.assertEquals(5, dt.getSecondOfMinute());
		Assert.assertEquals(123, dt.getMillisOfSecond());
	}
	
	@Test
	public void testyyyyMMddHHmmssfffm0600() {
		DateTime dt = InternalUtil.parseDateTime("2010-12-20T17:34:05.123-06:00");
		dt = dt.toDateTime(DateTimeZone.UTC);
		
		Assert.assertEquals(2010, dt.getYear());
		Assert.assertEquals(12, dt.getMonthOfYear());
		Assert.assertEquals(20, dt.getDayOfMonth());
		Assert.assertEquals(23, dt.getHourOfDay());
		Assert.assertEquals(34, dt.getMinuteOfHour());
		Assert.assertEquals(5, dt.getSecondOfMinute());
		Assert.assertEquals(123, dt.getMillisOfSecond());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testyyyyMMddHHmmssffm0600() {
		InternalUtil.parseDateTime("2010-12-20T17:34:05.12-06:00");
	}
	
	@Test
	public void testFormatDateTimeyyyyMMddHHmm() {
		LocalDateTime dt = new LocalDateTime(2010, 12, 20, 17, 34);
		Assert.assertEquals("2010-12-20T17:34", InternalUtil.formatDateTime(dt));
	}
	
	@Test
	public void testFormatDateTimeyyyyMMddHHmmss() {
		LocalDateTime dt = new LocalDateTime(2010, 12, 20, 17, 34, 5);
		Assert.assertEquals("2010-12-20T17:34:05", InternalUtil.formatDateTime(dt));
	}

	@Test
	public void testFormatDateTimeyyyyMMddHHmmssfffffff() {
		LocalDateTime dt = new LocalDateTime(2010, 12, 20, 17, 34, 5, 123);
		Assert.assertEquals("2010-12-20T17:34:05.1230000", InternalUtil.formatDateTime(dt));
	}
	
	@Test
	public void testFormatDateTimeOffsetyyyyMMddHHmm() {
		Chronology c = ISOChronology.getInstance(DateTimeZone.forOffsetHours(1));
		DateTime dt = new DateTime(2010, 12, 20, 17, 34, 0, 0, c);
		Assert.assertEquals("2010-12-20T17:34+01:00", InternalUtil.formatDateTimeOffset(dt));
	}
	
	@Test
	public void testFormatDateTimeOffsetyyyyMMddHHmmss() {
		Chronology c = ISOChronology.getInstance(DateTimeZone.forOffsetHours(1));
		DateTime dt = new DateTime(2010, 12, 20, 17, 34, 5, 0, c);
		Assert.assertEquals("2010-12-20T17:34:05+01:00", InternalUtil.formatDateTimeOffset(dt));
	}

	@Test
	public void testFormatDateTimeOffsetyyyyMMddHHmmssfffffff() {
		Chronology c = ISOChronology.getInstance(DateTimeZone.forOffsetHours(1));
		DateTime dt = new DateTime(2010, 12, 20, 17, 34, 5, 123, c);
		Assert.assertEquals("2010-12-20T17:34:05.1230000+01:00", InternalUtil.formatDateTimeOffset(dt));
	}

}
