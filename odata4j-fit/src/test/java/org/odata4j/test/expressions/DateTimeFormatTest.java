package org.odata4j.test.expressions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.core4j.Enumerable;
import org.core4j.Func;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.internal.InternalUtil;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.server.ODataServer;
import org.odata4j.test.AbstractRuntimeTest;

public class DateTimeFormatTest extends AbstractRuntimeTest {

  public DateTimeFormatTest(RuntimeFacadeType type) {
    super(type);
  }

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

  @Test
  public void testyyyyMMddHHmmssffm0600() {
    DateTime dt = InternalUtil.parseDateTime("2010-12-20T17:34:05.12-06:00");
    dt = dt.toDateTime(DateTimeZone.UTC);

    Assert.assertEquals(2010, dt.getYear());
    Assert.assertEquals(12, dt.getMonthOfYear());
    Assert.assertEquals(20, dt.getDayOfMonth());
    Assert.assertEquals(23, dt.getHourOfDay());
    Assert.assertEquals(34, dt.getMinuteOfHour());
    Assert.assertEquals(5, dt.getSecondOfMinute());
    Assert.assertEquals(120, dt.getMillisOfSecond());
  }

  @Test
  public void testyyyyMMddHHmmssfm0600() {
    DateTime dt = InternalUtil.parseDateTime("2010-12-20T17:34:05.1-06:00");
    dt = dt.toDateTime(DateTimeZone.UTC);

    Assert.assertEquals(2010, dt.getYear());
    Assert.assertEquals(12, dt.getMonthOfYear());
    Assert.assertEquals(20, dt.getDayOfMonth());
    Assert.assertEquals(23, dt.getHourOfDay());
    Assert.assertEquals(34, dt.getMinuteOfHour());
    Assert.assertEquals(5, dt.getSecondOfMinute());
    Assert.assertEquals(100, dt.getMillisOfSecond());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testyyyyMMddHHmmsspm0600() {
    InternalUtil.parseDateTime("2010-12-20T17:34:05.-06:00");
  }

  @Test
  public void testFormatDateTimeyyyyMMddHHmm() {
    LocalDateTime dt = new LocalDateTime(2010, 12, 20, 17, 34);
    Assert.assertEquals("2010-12-20T17:34:00", InternalUtil.formatDateTime(dt));
  }

  @Test
  public void testFormatDateTimeyyyyMMddHHmmss() {
    LocalDateTime dt = new LocalDateTime(2010, 12, 20, 17, 34, 5);
    Assert.assertEquals("2010-12-20T17:34:05", InternalUtil.formatDateTime(dt));
  }

  @Test
  public void testFormatDateTimeyyyyMMddHHmmssfffffff() {
    LocalDateTime dt = new LocalDateTime(2010, 12, 20, 17, 34, 5, 123);
    Assert.assertEquals("2010-12-20T17:34:05.123", InternalUtil.formatDateTime(dt));
  }

  @Test
  public void testFormatDateTimeOffsetyyyyMMddHHmm() {
    Chronology c = ISOChronology.getInstance(DateTimeZone.forOffsetHours(1));
    DateTime dt = new DateTime(2010, 12, 20, 17, 34, 0, 0, c);
    Assert.assertEquals("2010-12-20T17:34:00+01:00", InternalUtil.formatDateTimeOffset(dt));
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
    Assert.assertEquals("2010-12-20T17:34:05.123+01:00", InternalUtil.formatDateTimeOffset(dt));
  }

  @Test
  public void testDateTimeOffsetParseFormat() {
    dtoCheck(new DateTime(1967, 01, 02, 03, 04, 05, 123, DateTimeZone.UTC), "+00:00", 0);
    dtoCheck(new DateTime(1967, 01, 02, 03, 04, 05, 123, DateTimeZone.forOffsetHours(-7)), "-07:00", -7 * 60 * 60 * 1000);
    dtoCheck(new DateTime(1967, 01, 02, 03, 04, 05, 123, DateTimeZone.forOffsetHours(+5)), "+05:00", 5 * 60 * 60 * 1000);
    dtoCheck(new DateTime(1967, 01, 02, 03, 04, 05, 123, DateTimeZone.forOffsetHoursMinutes(3, 30)), "+03:30", ((3 * 60) + 30) * 60 * 1000);
  }

  private void dtoCheck(DateTime lhs, String tzS, int tzOffsetMillis) {
    // DateTime---->String
    Assert.assertTrue(lhs.getZone().getOffset(0) == tzOffsetMillis);
    String f = InternalUtil.formatDateTimeOffset(lhs);
    //System.out.println("lhs : " + f);
    Assert.assertTrue(f.endsWith(tzS));

    // back to DateTime
    DateTime utcp = InternalUtil.parseDateTime(f);
    f = InternalUtil.formatDateTimeOffset(lhs);
    //System.out.println("rhs: " + f);
    Assert.assertTrue(f.endsWith(tzS));

    // make sure the timezone was preserved.
    //System.out.println(" lhs zone: " + utc.getZone().getID() + " rhs zone: " + utcp.getZone().getID());
    Assert.assertTrue(utcp.getZone().getOffset(0) == tzOffsetMillis);
    Assert.assertEquals(lhs.getMillis(), utcp.getMillis());

    // zomg, DateTime.equals is all messed up...
  }

  @Test
  public void testDateTimeRoundtrip() {
    String endpointUri = "http://localhost:8810/DateTimeFormatTest.svc/";

    final long now = 1292865839424L;

    InMemoryProducer producer = new InMemoryProducer("DateTimeRoundtrip");

    producer.register(DateTimeRoundtrip.class, "DateTimeRoundtrip",
        new Func<Iterable<DateTimeRoundtrip>>() {
          @Override
          public Iterable<DateTimeRoundtrip> apply() {
            return Enumerable.create(new DateTimeRoundtrip(1, new Date(now)));
          }
        }, "Key");
    DefaultODataProducerProvider.setInstance(producer);
    ODataServer server = this.rtFacade.startODataServer(endpointUri);
    ODataConsumer c = this.rtFacade.create(endpointUri, null, null);
    List<OEntity> oentities = c.getEntities("DateTimeRoundtrip").execute().toList();

    Assert.assertEquals(1, oentities.size());
    // preserve milliseconds
    Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date(now)),
        oentities.get(0).getProperty("Date").getValue().toString());

    server.stop();
  }

  class DateTimeRoundtrip {
    public DateTimeRoundtrip(long key, Date date) {
      this.key = key;
      this.date = date;
    }

    public long key;
    public Date date;

    public long getKey() {
      return key;
    }

    public void setKey(long key) {
      this.key = key;
    }

    public Date getDate() {
      return date;
    }

    public void setDate(Date date) {
      this.date = date;
    }
  }
}
