
package org.odata4j.producer.custom;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.core.ORelatedEntitiesLinkInline;
import org.odata4j.core.ORelatedEntityLink;
import org.odata4j.format.FormatType;

public class CustomTestInheritance extends CustomTestBase {
  
  public CustomTestInheritance() {
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
  public void testGetEntityPolymorphic() {
    // GET an entity whose type is actually a subclass of the requested entity set.
    testGetEntity(FormatType.JSON);
  }
  
  @Test 
  public void testGetEntityPolymorphicAtom() {
    // GET an entity whose type is actually a subclass of the requested entity set.
    testGetEntity(FormatType.ATOM);
  }
  
  
  private void testGetEntity(FormatType ft) {
    ODataConsumer consumer = createConsumer(ft);
    
    //this.dumpResource("FileSystemItems('Dir-3')", ft);    
    //this.dumpResource("FileSystemItems('File-2-Dir-2')", ft);
    
    checkEntityType(consumer.getEntity("FileSystemItems", "Dir-3").execute());
    checkEntityType(consumer.getEntity("FileSystemItems", "File-2-Dir-2").execute());
  }
  
  @Test
  public void testGetInlineEntityPolymorphic() {
    // expand a nav prop where the associated entity is-a subclass of the nav props type.
    testGetInlineEntity(FormatType.JSON);
  }
  
  @Test
  public void testGetInlineEntityPolymorphicAtom() {
    // expand a nav prop where the associated entity is-a subclass of the nav props type.
    testGetInlineEntity(FormatType.ATOM);
  }
  
  private void testGetInlineEntity(FormatType ft) {
    ODataConsumer consumer = createConsumer(ft);
    
    this.dumpResource("FileSystemItems('Dir-3')?$expand=Items,NewestItem", ft);
    OEntity e = consumer.getEntity("FileSystemItems", "Dir-3").expand("Items,NewestItem").execute();
    checkEntityType(e);
    for (OEntity item : e.getLink("Items", ORelatedEntitiesLink.class).getRelatedEntities()) {
      checkEntityType(item);
    }
    checkEntityType(e.getLink("NewestItem", ORelatedEntityLink.class).getRelatedEntity());
  }
   
  @Test
  public void testGetEntitiesPolymorphic() {
    testGetEntities(FormatType.JSON);
  }
  
  @Test
  public void testGetEntitiesPolymorphicAtom() {
    testGetEntities(FormatType.ATOM);
  }
  
  private void testGetEntities(FormatType ft) {
    ODataConsumer consumer = createConsumer(ft);
    
    dumpResource("FileSystemItems", ft);
    // GET some entities whose types are actually a subclasses of the requested entity set.
    for (OEntity e : consumer.getEntities("FileSystemItems")) {
     checkEntityType(e);
    }
  }
  
  private void checkEntityType(OEntity e) {
    System.out.println("check entity: " + e.getEntityKey());
    String name = e.getProperty("Name", String.class).getValue();
    if (name.startsWith("Dir")) {
      assertTrue(e.getEntityType().getName().equals("Directory"));
    } else if (name.startsWith("File")) {
      assertTrue(e.getEntityType().getName().equals("File"));
    } else {
      assertTrue(false);
    }
  }
  
  @Test
  public void testGetInlineEntitiesPolymorphic() {
    // expand a nav prop where the associated entity is-a subclass of the nav props type.
    testGetInlineEntities(FormatType.JSON);
  }
  
  @Test
  public void testGetInlineEntitiesPolymorphicAtom() {
    // expand a nav prop where the associated entity is-a subclass of the nav props type.
    testGetInlineEntities(FormatType.ATOM);
  }
  
  private void testGetInlineEntities(FormatType ft) {
    ODataConsumer consumer = createConsumer(ft);

    this.dumpResourceJSON("Directories?$expand=Items");
    // GET some entities whose types are actually a subclasses of the requested entity set.
    for (OEntity e : consumer.getEntities("Directories").expand("Items")) {
      checkEntityType(e);
      ORelatedEntitiesLink l = e.getLink("Items", ORelatedEntitiesLinkInline.class);
      if (null != l.getRelatedEntities()) {
        for (OEntity i : l.getRelatedEntities()) {
          checkEntityType(i);
        }
      }
    }
  }
}
