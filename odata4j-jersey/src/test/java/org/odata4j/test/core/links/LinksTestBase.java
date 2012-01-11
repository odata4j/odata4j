package org.odata4j.test.core.links;

import javax.persistence.EntityManager;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OLink;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.core.ORelatedEntitiesLinkInline;
import org.odata4j.core.ORelatedEntityLink;
import org.odata4j.core.ORelatedEntityLinkInline;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;
import org.odata4j.jersey.examples.producer.JerseyProducerUtil;
import org.odata4j.producer.jpa.northwind.Orders;
import org.odata4j.producer.jpa.northwind.test.JPAProducerTestBase;
import org.odata4j.producer.resources.DefaultODataProducerProvider;

/**
 * Tests for new OLink semantics.
 */
public class LinksTestBase extends JPAProducerTestBase {

  protected static boolean useJpaProducer = false;

  @BeforeClass
  public static void beforeClass() throws Exception {

    if (useJpaProducer) {
      JPAProducerTestBase.setUpClass(50); // not annotated, must call manually

      entitySetName = "Orders";
      toOneNavPropName = "Customer";
      toManyNavPropName = "OrderDetails";
      emptyId = 333333;
      deferredId = 10248;
      populatedId = 10248;
      numberOfRelatedEntities = 3;
      insertData();
    } else {
      LinksProducer producer = new LinksProducer();

      DefaultODataProducerProvider.setInstance(producer);
      server = JerseyProducerUtil.startODataServer(endpointUri);
    }
  }

  @AfterClass
  public static void afterClass() throws Exception {
    if (useJpaProducer) {
      // JPAProducerTestBase.tearDownClass(); // this is annotated so it gets called.
    } else {
      if (server != null) {
        server.stop();
      }
    }
  }

  private static void insertData() {
    // need an entity with a to-0..1 nav prop and a to-* nav prop where the
    // associated entities are null or empty
    EntityManager em = null;
    try {
      em = emf.createEntityManager();
      em.getTransaction().begin();
      Orders o = new Orders((Integer) emptyId);
      em.persist(o);
      em.getTransaction().commit();

      Orders o1 = em.find(Orders.class, (Integer) emptyId);
      System.out.println("inserted empty order: " + o1.getOrderID());
      emptyId = o1.getOrderID();
    } finally {
      if (null != em) {
        if (em.getTransaction().isActive()) {
          try {
            em.getTransaction().rollback();
          } catch (Exception tex) {}
        }
        try {
          em.close();
        } catch (Exception ex) {}
      }
    }

  }

  public static String entitySetName = "As";
  public static String toOneNavPropName = "MyB";
  public static String toManyNavPropName = "MyCs";
  public static Object emptyId = "a0";
  public static Object deferredId = "a0";
  public static Object populatedId = "a1";
  public static int numberOfRelatedEntities = 2;

  protected void testReadDeferred(FormatType formatType) {
    ODataConsumer consumer = ODataJerseyConsumer.newBuilder(endpointUri).setFormatType(formatType).build();
    ;
    ODataConsumer.dump.all(true);
    OEntity a0 = consumer.getEntity(entitySetName, deferredId).execute();

    OLink b = a0.getLink(toOneNavPropName, OLink.class);
    Assert.assertTrue(b instanceof ORelatedEntityLink);
    Assert.assertFalse(b.isCollection());
    Assert.assertFalse(b.isInline());
    Assert.assertTrue(b.getRelatedEntity() == null);

    Assert.assertTrue(b.getRelatedEntities() == null);

    OLink cs = a0.getLink(toManyNavPropName, OLink.class);
    Assert.assertTrue(cs instanceof ORelatedEntitiesLink);
    Assert.assertTrue(cs.isCollection());
    Assert.assertFalse(cs.isInline());
    Assert.assertTrue(cs.getRelatedEntity() == null);
    Assert.assertTrue(cs.getRelatedEntities() == null);
  }

  protected void testReadEmpty(FormatType formatType) {
    ODataConsumer consumer = ODataJerseyConsumer.newBuilder(endpointUri).setFormatType(formatType).build();
    ;
    ODataConsumer.dump.all(true);
    OEntity a0 = consumer.getEntity(entitySetName, emptyId).expand(toOneNavPropName + "," + toManyNavPropName).execute();

    OLink b = a0.getLink(toOneNavPropName, OLink.class);
    Assert.assertTrue(b instanceof ORelatedEntityLinkInline);
    Assert.assertFalse(b.isCollection());
    Assert.assertTrue(b.isInline());
    Assert.assertTrue(b.getRelatedEntity() == null);
    Assert.assertTrue(b.getRelatedEntities() == null);

    OLink cs = a0.getLink(toManyNavPropName, OLink.class);
    Assert.assertTrue(cs instanceof ORelatedEntitiesLinkInline);
    Assert.assertTrue(cs.isCollection());
    Assert.assertTrue(cs.isInline());
    Assert.assertTrue(cs.getRelatedEntity() == null);
    Assert.assertTrue(cs.getRelatedEntities() == null || cs.getRelatedEntities().isEmpty());
  }

  protected void testReadPopulated(FormatType formatType) {
    ODataConsumer consumer = ODataJerseyConsumer.newBuilder(endpointUri).setFormatType(formatType).build();
    ;
    ODataConsumer.dump.all(true);
    OEntity a0 = consumer.getEntity(entitySetName, populatedId).expand(toOneNavPropName + "," + toManyNavPropName).execute();

    OLink b = a0.getLink(toOneNavPropName, OLink.class);
    Assert.assertTrue(b instanceof ORelatedEntityLinkInline);
    Assert.assertFalse(b.isCollection());
    Assert.assertTrue(b.isInline());
    Assert.assertTrue(b.getRelatedEntity() != null);
    Assert.assertTrue(b.getRelatedEntities() == null);

    OLink cs = a0.getLink(toManyNavPropName, OLink.class);
    Assert.assertTrue(cs instanceof ORelatedEntitiesLinkInline);
    Assert.assertTrue(cs.isCollection());
    Assert.assertTrue(cs.isInline());
    Assert.assertTrue(cs.getRelatedEntity() == null);
    Assert.assertTrue(cs.getRelatedEntities() != null && cs.getRelatedEntities().size() == numberOfRelatedEntities);
  }

}
