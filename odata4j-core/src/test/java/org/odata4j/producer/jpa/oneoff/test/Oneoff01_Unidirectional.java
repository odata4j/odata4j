package org.odata4j.producer.jpa.oneoff.test;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;

public class Oneoff01_Unidirectional extends OneoffTestBase {


	@BeforeClass
	public static void setUpClass() throws Exception {
		setUpClass(Oneoff01_Unidirectional.class,20);		
	}
	
	@Test
	public void unidirectional(){
		
		ODataConsumer consumer = ODataConsumer.create(endpointUri);
		
		OEntity category = consumer.createEntity("Categories01").execute();
		Assert.assertNotNull(category);
		System.out.println(category);
	}

	

}
