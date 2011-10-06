
package org.odata4j.producer.custom;

import java.util.List;
import org.odata4j.core.OSimpleObject;
import org.odata4j.core.OObject;
import org.odata4j.core.OCollection;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.core.OProperty;
import org.odata4j.core.OEntity;
import org.odata4j.consumer.ODataConsumer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.format.FormatType;
import static org.junit.Assert.*;

/**
 *
 */
public class CustomTest extends CustomTestBase {
  
  public CustomTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    setUpClass(50);
  }

  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }
  
 
  
  @Test
  public void testPropertiesJSON() {
    dumpResourceJSON("Type1s");
    testProperties(FormatType.JSON);
  }
  
  @Test
  public void testPropertiesAtom() {
    // TODO when the xml parsers/writers support Bag properties
    // testProperties(FormatType.ATOM);
  }
  
  
  private void testProperties(FormatType format) {
    ODataConsumer c = createConsumer(format);
    
    OEntity e = c.getEntity("Type1s", "0").execute();
    
     checkCollection(e.getProperty("EmptyStrings"), EdmSimpleType.STRING, new ValueGenerator() {

      @Override
      public Object getValue(int idx) {
        return null;
      }

      @Override
      public int getNExpected() {
        return 0;
      }
      
    });
     
    checkCollection(e.getProperty("BagOStrings"), EdmSimpleType.STRING, new ValueGenerator() {

      @Override
      public Object getValue(int idx) {
        return "bagstring-" + idx;
      }

      @Override
      public int getNExpected() {
        return 3;
      }
      
    });
    
    checkCollection(e.getProperty("ListOStrings"), EdmSimpleType.STRING, new ValueGenerator() {

      @Override
      public Object getValue(int idx) {
        return "liststring-" + idx;
      }

      @Override
      public int getNExpected() {
        return 5;
      }
      
    });
    
    checkCollection(e.getProperty("BagOInts"), EdmSimpleType.INT32, new ValueGenerator() {

      @Override
      public Object getValue(int idx) {
        return idx;
      }

      @Override
      public int getNExpected() {
        return 5;
      }
      
    });
    
    
    OProperty<?> cx = e.getProperty("Complex1");
    assertTrue(cx.getType().getFullyQualifiedTypeName().equals("myns.ComplexType1"));
    List<OProperty<?>> props = (List<OProperty<?>>) cx.getValue(); // uggh...why isn't this an OComplexObject?
    assertTrue(props.size() == 2);
    OProperty<?> prop = findProp("Prop1", props);
    assertTrue(null != prop);
    assertTrue(prop.getValue() instanceof String);
    assertTrue(((String)prop.getValue()).equals("Val1"));
    prop = findProp("Prop2", props);
    assertTrue(null != prop);
    assertTrue(prop.getValue() instanceof String);
    assertTrue(((String)prop.getValue()).equals("Val2"));
    
    OProperty<?> ccx = e.getProperty("ListOComplex");
    assertTrue(ccx.getType() instanceof EdmCollectionType);
    EdmCollectionType ct = (EdmCollectionType) ccx.getType();
    assertTrue(ct.getCollectionType().getFullyQualifiedTypeName().equals("myns.ComplexType1"));
    assertTrue(((OCollection)ccx.getValue()).size() == 2);
  }
  
  private OProperty<?> findProp(String name, List<OProperty<?>> props) {
    for (OProperty<?> p : props) {
      if (name.equals(p.getName())) {
        return p;
      }
    }
    return null;
  }
  
  private static interface ValueGenerator {
    int getNExpected();
    Object getValue(int idx);
  }
  
  private void checkCollection(OProperty<?> prop, EdmSimpleType itemType, ValueGenerator vg) {
    //OProperty<?> prop = e.getProperty("BagOStrings");
    assertTrue(null != prop);
    assertTrue(prop.getType() instanceof EdmCollectionType);
    EdmCollectionType ct = (EdmCollectionType) prop.getType();
    assertTrue(ct.getCollectionType().equals(itemType));
    OCollection<? extends OObject> coll = (OCollection<? extends OObject>) prop.getValue();
    assertTrue(coll.size() == vg.getNExpected());
    int idx = 0;
    for (OObject obj : coll) {
      assertTrue(obj.getType().equals(itemType));
      assertTrue(((OSimpleObject)obj).getValue().equals(vg.getValue(idx)));
      idx += 1;
    }
  }
}
