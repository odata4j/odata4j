package org.odata4j.test.expression;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.core4j.Funcs;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.expression.ExpressionParser;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.ODataProducerProvider;
import org.odata4j.producer.resources.ODataResourceConfig;
import org.odata4j.producer.server.JerseyServer;

public class ScenarioTest {

    @Test
    public void testScenario() {

        ExpressionParser.DUMP_EXPRESSION_INFO = true;

        String uri = "http://localhost:18888/";

        InMemoryProducer producer = new InMemoryProducer("ScenarioTest");
        ODataProducerProvider.setInstance(producer);

        JerseyServer server = new JerseyServer(uri);
        server.addAppResourceClasses(new ODataResourceConfig().getClasses());
        server.start();

        ODataConsumer c = ODataConsumer.create(uri);
        Assert.assertEquals(0, c.getEntitySets().count());

        List<Foo> foos = new ArrayList<Foo>();
        producer.register(Foo.class, String.class, "Foos1", Funcs.constant((Iterable<Foo>) foos), "Id");
        Assert.assertEquals(1, c.getEntitySets().count());

        Assert.assertEquals(0, c.getEntities("Foos1").execute().count());
        foos.add(new Foo("1", 3, 3, "Alpha"));
        foos.add(new Foo("2", 2, 2, null));
        foos.add(new Foo("3", 1, 1, "Gamma"));
        Assert.assertEquals(3, c.getEntities("Foos1").execute().count());
        Assert.assertEquals(1, c.getEntities("Foos1").top(1).execute().count());
        Assert.assertEquals("1", c.getEntities("Foos1").top(1).execute().first().getProperties().get(0).getValue());
        Assert.assertEquals(2, c.getEntities("Foos1").skip(1).execute().count());
        Assert.assertEquals("2", c.getEntities("Foos1").skip(1).top(1).execute().first().getProperties().get(0).getValue());
        Assert.assertEquals(0, c.getEntities("Foos1").top(0).execute().count());
        Assert.assertEquals("3", c.getEntities("Foos1").filter("Id eq '3'").execute().first().getProperties().get(0).getValue());
        Assert.assertEquals("3", c.getEntities("Foos1").filter("true and Id eq '3'").execute().first().getProperties().get(0).getValue());
        Assert.assertEquals(0, c.getEntities("Foos1").filter("Id ne Id").execute().count());
        Assert.assertEquals(3, c.getEntities("Foos1").filter("true or false").execute().count());
        Assert.assertEquals("3", c.getEntities("Foos1").orderBy("Id desc").top(1).execute().first().getProperties().get(0).getValue());

        Assert.assertEquals("3", c.getEntities("Foos1").orderBy("Id desc, Int32").top(1).execute().first().getProperties().get(0).getValue());
        Assert.assertEquals(1, c.getEntities("Foos1").filter("Int32 eq 2").execute().count());
        Assert.assertEquals(1, c.getEntities("Foos1").filter("Int32 gt 2").execute().count());
        Assert.assertEquals(1, c.getEntities("Foos1").filter("Int64 eq 2").execute().count());
        Assert.assertEquals(3, c.getEntities("Foos1").filter("Int32 lt " + Integer.MAX_VALUE + 100).execute().count());
        Assert.assertEquals(2, c.getEntities("Foos1").filter("Int32 ge 2").execute().count());
        Assert.assertEquals(2, c.getEntities("Foos1").filter("Int32 le 2").execute().count());
        Assert.assertEquals(1, c.getEntities("Foos1").filter("Int32 add 2 sub 2 eq 2 ").execute().count());
        Assert.assertEquals(1, c.getEntities("Foos1").filter("Int32 mul 3 eq 6 ").execute().count());
        Assert.assertEquals(1, c.getEntities("Foos1").filter("Int32 div 1 eq 2 ").execute().count());
        Assert.assertEquals(1, c.getEntities("Foos1").filter("(((Int32 mul 6) div 2) div 3) eq 2 ").execute().count());
        Assert.assertEquals(2, c.getEntities("Foos1").filter("Int32 mod 2 eq 1").execute().count());
        Assert.assertEquals(2, c.getEntities("Foos1").filter("not (Int32 eq 2)").execute().count());
        Assert.assertEquals(0, c.getEntities("Foos1").filter("Id eq null").execute().count());
        Assert.assertEquals(3, c.getEntities("Foos1").filter("null eq null").execute().count());
        Assert.assertEquals(1, c.getEntities("Foos1").filter("Name eq null").execute().count());
        Assert.assertEquals(1, c.getEntities("Foos1").filter("substringof('lph',Name)").execute().count());

        server.stop();

    }

    public static class Foo {
        
        private final String id;
        private final int int32;
        private final int int64;
        private final String name;

        public Foo(String id, int int32, int int64, String name) {
            this.id = id;
            this.int32 = int32;
            this.int64 = int64;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public int getInt32() {
            return int32;
        }

        public int getInt64() {
            return int64;
        }

        public String getName() {
            return name;
        }
    }
}
