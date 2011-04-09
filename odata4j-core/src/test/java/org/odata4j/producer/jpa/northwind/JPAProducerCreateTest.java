package org.odata4j.producer.jpa.northwind;

import org.core4j.Enumerable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OClientBehaviors;
import org.odata4j.core.OEntity;
import org.odata4j.core.OPredicates;
import org.odata4j.core.OProperties;
import org.odata4j.format.FormatType;

public class JPAProducerCreateTest extends JPAProducerTestBase {
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		setUpClass(20);
	}

	@Test
	public void tunneledInsertEntityToExistingEntityRelationAtom() {
		ODataConsumer consumer = ODataConsumer.create(endpointUri, OClientBehaviors.methodTunneling("PUT"));		
		insertEntityToExistingEntityRelationAndTest(consumer);
	}

	@Test
	public void tunneledInsertEntityToExistingEntityRelationJson() {
		ODataConsumer consumer = ODataConsumer.create(FormatType.JSON, endpointUri, OClientBehaviors.methodTunneling("PUT"));
		insertEntityToExistingEntityRelationAndTest(consumer);
	}

	@Test
	public void insertEntityToExistingEntityRelationAtom() {
		ODataConsumer consumer = ODataConsumer.create(endpointUri);
		insertEntityToExistingEntityRelationAndTest(consumer);
	}

	@Test
	public void insertEntityToExistingEntityRelationJson() {
		ODataConsumer consumer = ODataConsumer.create(FormatType.JSON, endpointUri);
		insertEntityToExistingEntityRelationAndTest(consumer);
	}

	@Test
	public void tunneledInsertEntityUsingLinksAtom() {
		ODataConsumer consumer = ODataConsumer.create(endpointUri, OClientBehaviors.methodTunneling("PUT"));
		insertEntityUsingLinksAndTest(consumer);
	}

	@Test
	public void tunneledInsertEntityUsingLinksJson() {
		ODataConsumer consumer = ODataConsumer.create(FormatType.JSON, endpointUri, OClientBehaviors.methodTunneling("PUT"));
		insertEntityUsingLinksAndTest(consumer);
	}

	@Test
	public void insertEntityUsingLinksAtom() {
		ODataConsumer consumer = ODataConsumer.create(endpointUri);
		insertEntityUsingLinksAndTest(consumer);
	}
	
	@Test
	public void insertEntityUsingLinksJson() {
		ODataConsumer consumer = ODataConsumer.create(FormatType.JSON, endpointUri);
		insertEntityUsingLinksAndTest(consumer);
	}

	@Test
	public void insertEntityWithInlinedEntitiesAtom() {
		ODataConsumer consumer = ODataConsumer.create(endpointUri);
		insertEntityWithInlinedEntities(consumer);
	}
	
	@Test
	public void insertEntityWithInlinedEntitiesJson() {
		ODataConsumer consumer = ODataConsumer.create(FormatType.JSON, endpointUri);
		insertEntityWithInlinedEntities(consumer);
	}

	protected void insertEntityWithInlinedEntities(ODataConsumer consumer) {

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
	public void insertEntityWithInlinedEntityAtom() {
		insertEntityWithInlinedEntity(ODataConsumer.create(endpointUri));
	}
	
//	@Test
	public void insertEntityWithInlinedEntityJson() {
		insertEntityWithInlinedEntity(ODataConsumer.create(FormatType.JSON, endpointUri));
	}

	public void insertEntityWithInlinedEntity(ODataConsumer consumer) {
		ODataConsumer.dump.all(true);

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
