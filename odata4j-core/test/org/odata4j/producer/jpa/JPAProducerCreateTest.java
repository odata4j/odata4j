package org.odata4j.producer.jpa;

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
}
