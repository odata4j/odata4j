package org.odata4j.producer.jpa.oneoff01;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.producer.jpa.oneoff.OneoffTestBase;

public class Oneoff01_Unidirectional extends OneoffTestBase {


	@BeforeClass
	public static void setUpClass() throws Exception {
		setUpClass(Oneoff01_Unidirectional.class,20);		
	}
	
	@Test
    public void createOnetoManyUniDirectional() {
          final long now = System.currentTimeMillis();
          ODataConsumer consumer = ODataConsumer.create(endpointUri);
         
          OEntity comment = consumer
	          .createEntity("Comment")
	          .properties(OProperties.string("Description", "C1" + now))
	          .get();                
         
          OEntity ticket = consumer.createEntity("Ticket")
	          .properties(OProperties.string("Description", "T" + now))
	          .inline("Comments", comment)
	          .execute();
          
          Assert.assertNotNull(ticket);
          ORelatedEntitiesLink link = ticket.getLink("Comments",ORelatedEntitiesLink.class);
          Assert.assertEquals(1,consumer.getEntities(link).execute().count());
         
    }
}
