package org.odata4j.test.expression;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.ODataProducerProvider;
import org.odata4j.producer.resources.ODataResourceConfig;
import org.odata4j.producer.server.JerseyServer;

import core4j.Func;
import core4j.Funcs;

public class ScenarioTest {

	@Test
	public void testScenario(){
		
		String uri = "http://localhost:18888/";
		
		InMemoryProducer producer = new InMemoryProducer("Test");
		ODataProducerProvider.setInstance(producer);
		
		
		JerseyServer server = new JerseyServer(uri);
		server.addAppResourceClass(new ODataResourceConfig().getClasses());
		server.start();
		
		ODataConsumer c = ODataConsumer.create(uri);
		Assert.assertEquals(0,c.getEntitySets().count());
		
		List<Foo> foos = new ArrayList<Foo>();
		producer.register(Foo.class, String.class, "Foos1", Funcs.constant((Iterable<Foo>)foos), "Id");
		Assert.assertEquals(1,c.getEntitySets().count());
		
		Assert.assertEquals(0,c.getEntities("Foos1").execute().count());
		foos.add(new Foo("1"));
		foos.add(new Foo("2"));
		foos.add(new Foo("3"));
		Assert.assertEquals(3,c.getEntities("Foos1").execute().count());
		Assert.assertEquals(1,c.getEntities("Foos1").top(1).execute().count());
		Assert.assertEquals("1",c.getEntities("Foos1").top(1).execute().first().getProperties().get(0).getValue());
		Assert.assertEquals(2,c.getEntities("Foos1").skip(1).execute().count());
		Assert.assertEquals("2",c.getEntities("Foos1").skip(1).top(1).execute().first().getProperties().get(0).getValue());
		Assert.assertEquals(0,c.getEntities("Foos1").top(0).execute().count());
		Assert.assertEquals("3",c.getEntities("Foos1").filter("Id eq '3'").execute().first().getProperties().get(0).getValue());
		Assert.assertEquals("3",c.getEntities("Foos1").filter("true and Id eq '3'").execute().first().getProperties().get(0).getValue());
		Assert.assertEquals(0,c.getEntities("Foos1").filter("Id ne Id").execute().count());
		Assert.assertEquals(3,c.getEntities("Foos1").filter("true or false").execute().count());
		Assert.assertEquals("3",c.getEntities("Foos1").orderBy("Id desc").top(1).execute().first().getProperties().get(0).getValue());
		server.stop();
		
		
	}

	
	private static class Foo {
		private final String id;
		public Foo(String id){
			this.id = id;
		}
		public String getId(){
			return id;
		}
	}
}
