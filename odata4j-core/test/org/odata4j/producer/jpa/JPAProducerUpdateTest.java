package org.odata4j.producer.jpa;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.behaviors.MethodTunnelingBehavior;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;

public class JPAProducerUpdateTest extends JPAProducerTestBase {

	@BeforeClass
	public static void setUpClass() throws Exception {
		setUpClass(20);
	}

	@Test
	public void tunneledUpdateEntity() {
		ODataConsumer consumer = ODataConsumer.create(endpointUri, new MethodTunnelingBehavior("PUT"));
		
		updateEntityAndTest(consumer);
	}
	
	@Test
	public void updateEntity() {
		ODataConsumer consumer = ODataConsumer.create(endpointUri);
		
		updateEntityAndTest(consumer);
	}

	protected void updateEntityAndTest(ODataConsumer consumer) {
		OEntity customer = consumer.getEntity("Customers", "ALFKI").execute();
		
		boolean ret = consumer
				.updateEntity(customer, "Customers", "ALFKI")
				.properties(OProperties.string("ContactName", "Maria Gleich"))
				.execute();
		Assert.assertTrue(ret);
		
		customer = consumer.getEntity("Customers", "ALFKI").execute();
		Assert.assertEquals("Maria Gleich", customer.getProperty("ContactName").getValue());
		Assert.assertEquals("Alfreds Futterkiste", customer.getProperty("CompanyName").getValue());
	}
	
}
