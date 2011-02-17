package org.odata4j.producer.jpa;

import org.core4j.Enumerable;
import org.core4j.Predicate1;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.behaviors.MethodTunnelingBehavior;
import org.odata4j.core.OEntity;
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
		
		OEntity prod1 = consumer
			.createEntity("Products")
    		.properties(OProperties.string("ProductName", "Healthy Drink"))
    		.properties(OProperties.boolean_("Discontinued", true))
			.get();
		OEntity prod2 = consumer
    		.createEntity("Products")
    		.properties(OProperties.string("ProductName", "Tasty Drink"))
    		.properties(OProperties.boolean_("Discontinued", false))
    		.get();
		
		OEntity category = consumer
			.createEntity("Categories")
			.properties(OProperties.string("CategoryName", "Fancy Beverages"))
			.inline("Products", prod1, prod2)
			.execute();
		
		Assert.assertNotNull(category);
		Assert.assertNotNull(category.getProperty("CategoryID").getValue());
		Assert.assertEquals("Fancy Beverages", category.getProperty("CategoryName").getValue());
		
		Enumerable<OEntity> products = consumer
			.getEntities("Categories")
			.nav(category.getProperty("CategoryID").getValue(), "Products")
			.execute();
		
		Assert.assertEquals(2, products.count());
		
		prod1 = products.where(new Predicate1<OEntity>() {
			@Override
			public boolean apply(OEntity input) {
				return "Healthy Drink".equals(input.getProperty("ProductName").getValue());
			}
		}).firstOrNull();
		Assert.assertNotNull(prod1);
		Assert.assertNotNull(prod1.getProperty("ProductID").getValue());
		Assert.assertEquals(true, prod1.getProperty("Discontinued").getValue());
		
		prod2 = products.where(new Predicate1<OEntity>() {
			@Override
			public boolean apply(OEntity input) {
				return "Tasty Drink".equals(input.getProperty("ProductName").getValue());
			}
		}).firstOrNull();
		Assert.assertNotNull(prod2);
		Assert.assertNotNull(prod2.getProperty("ProductID").getValue());
		Assert.assertEquals(false, prod2.getProperty("Discontinued").getValue());
	}
	
	
	protected void insertEntityToExistingEntityRelationAndTest(
			ODataConsumer consumer) {
		OEntity category = consumer.getEntity("Categories", 1).execute();
		
		Assert.assertNotNull(category);
		OEntity products = consumer
			.createEntity("Products")
			.properties(OProperties.string("ProductName", "Healthy Drink"))
			.properties(OProperties.boolean_("Discontinued", false))
			.addToRelation(category, "Products").execute();

		Object id = products.getProperty("ProductID").getValue();
		Assert.assertNotNull(id);
		
		products = consumer.getEntity("Products", id).execute();
		Assert.assertEquals(id, products.getProperty("ProductID").getValue());
		Assert.assertEquals("Healthy Drink", products.getProperty("ProductName").getValue());
	}
	
	protected void insertEntityUsingLinksAndTest(
			ODataConsumer consumer) {
		
		OEntity category = consumer.getEntity("Categories", 1).execute();
		
		Assert.assertNotNull(category);

		OEntity product = consumer
    		.createEntity("Products")
    		.properties(OProperties.string("ProductName", "Healthy Drink"))
    		.properties(OProperties.boolean_("Discontinued", true))
    		.link("Category", category)
    		.execute();
		
		Assert.assertNotNull(product);
		Assert.assertNotNull(product.getProperty("ProductID").getValue());
		Assert.assertEquals(1, product.getProperty("CategoryID").getValue());
		Assert.assertEquals("Healthy Drink", product.getProperty("ProductName").getValue());
		Assert.assertEquals(true, product.getProperty("Discontinued").getValue());
		
		Object key = product.getProperty("ProductID").getValue();
		product = consumer
			.getEntity("Products", key)
			.execute();

		Assert.assertNotNull(product);
		Assert.assertEquals(key, product.getProperty("ProductID").getValue());
		Assert.assertEquals(1, product.getProperty("CategoryID").getValue());
		Assert.assertEquals("Healthy Drink", product.getProperty("ProductName").getValue());
		Assert.assertEquals(true, product.getProperty("Discontinued").getValue());
	}
 
}
