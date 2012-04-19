package org.odata4j.test.integration.expressions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.core4j.Funcs;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.*;
import org.odata4j.format.FormatType;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.server.ODataServer;
import org.odata4j.test.integration.AbstractRuntimeTest;
import org.odata4j.test.integration.expressions.PojoWithAllTypesComplex.Complex1;
import org.odata4j.test.integration.expressions.PojoWithAllTypesComplex.Complex2;

public class JsonTest extends AbstractRuntimeTest {

  public JsonTest(RuntimeFacadeType type) {
    super(type);
  }

  @Test
  public void testJsonEntity() {

    try {
      setup();
      // did the properties round trip ok?
      OEntity e = consumer.getEntity("Pojo", (int)1).execute();
      assertPojoEqualsOEntity(pojo, e.getProperties());
      
    } finally {
      server.stop();
    }

  }
  
  public void testJsonCollection() {

    try {
      setup();
      // hmmh, apparently the consumer client is not capable of somethign like this?
      OEntity e = consumer.getEntity("Pojo/Complexes", (int)1).execute();
      assertPojoEqualsOEntity(pojo, e.getProperties());
      
    } finally {
      
      server.stop();
    }

  }
  
  private PojoWithAllTypesComplex pojo;
  private ODataServer server;
  private ODataConsumer consumer;
  
  private void setup() {
    String uri = "http://localhost:18890/TestService.svc/";

    InMemoryProducer producer = new InMemoryProducer("JsonTest");
    DefaultODataProducerProvider.setInstance(producer);

    server = this.rtFacade.startODataServer(uri);

    consumer = this.rtFacade.createODataConsumer(uri, FormatType.JSON, null);
    Assert.assertEquals(0, consumer.getEntitySets().count());

    // register a complex type:
    producer.registerComplexType(PojoWithAllTypes.class, "PojoWithAllTypes");
    producer.registerComplexType(PojoWithAllTypesComplex.Complex2.class, "Complex2");
    producer.registerComplexType(PojoWithAllTypesComplex.Complex1.class, "Complex1");


    List<PojoWithAllTypesComplex> pojos = new ArrayList<PojoWithAllTypesComplex>();
    producer.register(PojoWithAllTypesComplex.class, "Pojo", Funcs.constant((Iterable<PojoWithAllTypesComplex>) pojos), "Int32");

    List<String> stringList = new ArrayList<String>();
    stringList.add("tag1");
    stringList.add("tag2");
    stringList.add("tag3");

    PojoWithAllTypes embeddedPojo =
            new PojoWithAllTypes(new byte[]{0x04, 0x05, 0x06}, false, UnsignedByte.valueOf(0xEE), (byte) -0x04, new LocalDateTime(), new BigDecimal("223.456"), 223.456,
            Guid.randomGuid(), (short) 124, 2, Long.MAX_VALUE - 1, 124.456F, "JohnEmbedded", new LocalTime(), new DateTime());

    pojo =
            new PojoWithAllTypesComplex(new byte[]{0x01, 0x02, 0x03}, true, UnsignedByte.valueOf(0xFF), (byte) -0x05, new LocalDateTime(), new BigDecimal("123.456"), 123.456,
            Guid.randomGuid(), (short) 123, 1, Long.MAX_VALUE, 123.456F, "John", new LocalTime(), new DateTime(), stringList, embeddedPojo);
    pojo.addComplex1(new Complex1("c1a", "c1b", new Complex2("c2a", "c2b"), null, null)).addComplex1(new Complex1("c2a", "c2b", null, Arrays.asList(new Complex2[]{new Complex2("cc2a", "cc2b")}), Arrays.asList(new String[]{"es1", "es2"})));
    pojos.add(pojo);


    {
      String output = this.rtFacade.getWebResource(uri + "$metadata");
      System.out.println(output);
    }
    String output = this.rtFacade.getWebResource(uri + "Pojo?$format=json");
    System.out.println(output);


  }
  
  private static Object getPropertyValue(String name, List<OProperty<?>> props) {
    for (OProperty<?> p : props) {
      if (p.getName().equals(name)) {
        return p.getValue();
      }
    }
    return null;
  }

  private static void assertPojoEqualsOEntity(PojoWithAllTypes pojo, List<OProperty<?>> props) {
    Assert.assertEquals(pojo.getBoolean(), getPropertyValue("Boolean", props));
    // TODO when Edm.Binary supported by InMemoryProducer
    // assertArrayEquals(pojo.getBinary(), (byte[])getPropertyValue("Binary", props));
    Assert.assertEquals(pojo.getByte(), getPropertyValue("Byte", props));
    Assert.assertEquals(pojo.getDateTime(), getPropertyValue("DateTime", props));
    Assert.assertTrue(pojo.getDecimal().compareTo((BigDecimal) getPropertyValue("Decimal", props)) == 0);
    Assert.assertEquals(pojo.getDouble(), getPropertyValue("Double", props));
    Assert.assertEquals(pojo.getGuid(), getPropertyValue("Guid", props));
    Assert.assertEquals(pojo.getInt16(), getPropertyValue("Int16", props));
    Assert.assertEquals(pojo.getInt32(), getPropertyValue("Int32", props));
    Assert.assertEquals(pojo.getInt64(), getPropertyValue("Int64", props));
    Assert.assertEquals(pojo.getSingle(), getPropertyValue("Single", props));
    Assert.assertEquals(pojo.getString(), getPropertyValue("String", props));
    Assert.assertEquals(pojo.getTime(), getPropertyValue("Time", props));
    Assert.assertTrue(pojo.getDateTimeOffset().isEqual((DateTime) getPropertyValue("DateTimeOffset", props)));

    if (pojo instanceof PojoWithAllTypesComplex) {

      // embedded EdmComplexType
      PojoWithAllTypesComplex pojoC = (PojoWithAllTypesComplex) pojo;
      assertPojoEqualsOEntity(pojoC.getComplexType(), (List<OProperty<?>>) getPropertyValue("ComplexType", props));

      // embedded collection(Edm.String)
      OCollection<OSimpleObject> scollection = (OCollection<OSimpleObject>) getPropertyValue("StringList", props);
      Assert.assertEquals(scollection.size(), pojoC.getStringList().size());
      for (String sl : pojoC.getStringList()) {
        boolean found = false;
        for (OSimpleObject so : scollection) {
          if (((String)so.getValue()).equals(sl)) { found = true; break; }
        }
        Assert.assertTrue(found);
      }

      // embedded collection(Edm.ComplexType)
      OCollection<OComplexObject> ccollection = (OCollection<OComplexObject>) getPropertyValue("Complexes", props);
      Assert.assertEquals(ccollection.size(), pojoC.getComplexes().size());
      for (Complex1 c1 : pojoC.getComplexes()) {
        boolean found = false;
        for (OComplexObject co : ccollection) {
          if (((String)getPropertyValue("S1", co.getProperties())).equals(c1.getS1()) &&
              ((String)getPropertyValue("S2", co.getProperties())).equals(c1.getS2())) { 
            found = true; break; 
          }
        }
        Assert.assertTrue(found);
      }
    }
  }
  
  private static void assertArrayEquals(byte[] a, byte[] b) {
    Assert.assertEquals(a.length, b.length);
    for (int i = 0; i < a.length; i++) {
      Assert.assertEquals(a[i], b[i]);
    }
  }
}
