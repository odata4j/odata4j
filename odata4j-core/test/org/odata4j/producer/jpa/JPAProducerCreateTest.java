package org.odata4j.producer.jpa;

import org.core4j.Enumerable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.behaviors.MethodTunnelingBehavior;
import org.odata4j.core.OEntity;
import org.odata4j.core.OPredicates;
import org.odata4j.core.OProperties;

public class JPAProducerCreateTest extends JPAProducerTestBase {
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		setUpClass(20);
	}

	@Test
	public void tunneledInsertEntityToExistingEntityRelation() {
		ODataConsumer consumer = ODataConsumer.create(endpointUri, new MethodTunnelingBehavior("PUT"));
		
		insertEntityToExistingEntityRelationAndTest(consumer);
	}

	@Test
	public void insertEntityToExistingEntityRelation() {
		ODataConsumer consumer = ODataConsumer.create(endpointUri);
		
		insertEntityToExistingEntityRelationAndTest(consumer);
	}

	@Test
	public void tunneledInsertEntityUsingLinks() {
		ODataConsumer consumer = ODataConsumer.create(endpointUri, new MethodTunnelingBehavior("PUT"));
		
		insertEntityUsingLinksAndTest(consumer);
	}

	@Test
	public void insertEntityUsingLinks() {
		ODataConsumer consumer = ODataConsumer.create(endpointUri);
		
		insertEntityUsingLinksAndTest(consumer);
	}
	
	@Test
	public void insertEntityWithInlinedEntities() {
		ODataConsumer consumer = ODataConsumer.create(endpointUri);
		
		final long now = System.currentTimeMillis();
		OEntity prod1 = consumer
			.createEntity("Products")
    		.properties(OProperties.string("ProductName", "P1" + now))
    		.properties(OProperties.boolean_("Discontinued", true))
			.get();
		OEntity prod2 = consumer
    		.createEntity("Products")
    		.properties(OProperties.string("ProductName", "P2" + now))
    		.properties(OProperties.boolean_("Discontinued", false))
    		.get();
		
		OEntity category = consumer
			.createEntity("Categories")
			.properties(OProperties.string("CategoryName", "C" + now))
			.inline("Products", prod1, prod2)
			.execute();
		
		Assert.assertNotNull(category);
		Assert.assertNotNull(category.getProperty("CategoryID").getValue());
		Assert.assertEquals("C" + now, category.getProperty("CategoryName").getValue());
		
		Enumerable<OEntity> products = consumer
			.getEntities("Categories")
			.nav(category.getProperty("CategoryID").getValue(), "Products")
			.execute();
		
		Assert.assertEquals(2, products.count());
		
		prod1 = products.where(OPredicates.entityPropertyValueEquals("ProductName", "P1" + now)).firstOrNull();
		Assert.assertNotNull(prod1);
		Assert.assertNotNull(prod1.getProperty("ProductID").getValue());
		Assert.assertEquals(true, prod1.getProperty("Discontinued").getValue());
		
		prod2 = products.where(OPredicates.entityPropertyValueEquals("ProductName","P2" + now)).firstOrNull();
		Assert.assertNotNull(prod2);
		Assert.assertNotNull(prod2.getProperty("ProductID").getValue());
		Assert.assertEquals(false, prod2.getProperty("Discontinued").getValue());
	}
	
	@Test
	public void insertEntityWithInlinedEntity() {
		ODataConsumer consumer = ODataConsumer.create(endpointUri);

		final long now = System.currentTimeMillis();
		OEntity category = consumer
    		.createEntity("Categories")
    		.properties(OProperties.string("CategoryName", "C" + now))
    		.get();

		OEntity product = consumer.createEntity("Products")
    		.properties(OProperties.string("ProductName", "P" + now))
    		.properties(OProperties.boolean_("Discontinued", true))
    		.inline("Category", category)
    		.execute();
		
		Object id = product.getProperty("ProductID").getValue();
		Assert.assertNotNull(id);
		Assert.assertEquals("P" + now, product.getProperty("ProductName").getValue());
		Assert.assertEquals(true, product.getProperty("Discontinued").getValue());
		Object categoryId = product.getProperty("CategoryID").getValue();
		Assert.assertNotNull(categoryId);

		category = consumer.getEntity("Categories", categoryId).execute();
		Assert.assertEquals("C" + now, category.getProperty("CategoryName").getValue());
	}
		
	protected void insertEntityToExistingEntityRelationAndTest(ODataConsumer consumer) {
		OEntity category = consumer.getEntity("Categories", 1).execute();
		
		final long now = System.currentTimeMillis();
		Assert.assertNotNull(category);
		OEntity product = consumer
			.createEntity("Products")
			.properties(OProperties.string("ProductName", "P" + now))
			.properties(OProperties.boolean_("Discontinued", false))
			.addToRelation(category, "Products").execute();

		Object id = product.getProperty("ProductID").getValue();
		Assert.assertNotNull(id);
		
		product = consumer.getEntity("Products", id).execute();
		Assert.assertEquals(id, product.getProperty("ProductID").getValue());
		Assert.assertEquals("P" + now, product.getProperty("ProductName").getValue());
	}
	
	protected void insertEntityUsingLinksAndTest(ODataConsumer consumer) {
		
		OEntity category = consumer.getEntity("Categories", 1).execute();
		
		Assert.assertNotNull(category);

		final long now = System.currentTimeMillis();
		OEntity product = consumer
    		.createEntity("Products")
    		.properties(OProperties.string("ProductName", "P" + now))
    		.properties(OProperties.boolean_("Discontinued", true))
    		.link("Category", category)
    		.execute();
		
		Assert.assertNotNull(product);
		Assert.assertNotNull(product.getProperty("ProductID").getValue());
		Assert.assertEquals(1, product.getProperty("CategoryID").getValue());
		Assert.assertEquals("P" + now, product.getProperty("ProductName").getValue());
		Assert.assertEquals(true, product.getProperty("Discontinued").getValue());
		
		Object key = product.getProperty("ProductID").getValue();
		product = consumer
			.getEntity("Products", key)
			.execute();

		Assert.assertNotNull(product);
		Assert.assertEquals(key, product.getProperty("ProductID").getValue());
		Assert.assertEquals(1, product.getProperty("CategoryID").getValue());
		Assert.assertEquals("P" + now, product.getProperty("ProductName").getValue());
		Assert.assertEquals(true, product.getProperty("Discontinued").getValue());
	}
 
}
