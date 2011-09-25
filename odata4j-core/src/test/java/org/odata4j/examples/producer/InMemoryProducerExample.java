package org.odata4j.examples.producer;

import java.net.URL;
import java.util.Map.Entry;

import org.core4j.Enumerable;
import org.core4j.Enumerables;
import org.core4j.Func;
import org.core4j.Func1;
import org.core4j.Funcs;
import org.core4j.ThrowingFunc;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.ODataProducerProvider;

public class InMemoryProducerExample {

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static void main(String[] args) {

    String endpointUri = "http://localhost:8887/InMemoryProducerExample.svc/";

    // InMemoryProducer is a readonly odata provider that serves up POJOs as entities using bean properties
    // call InMemoryProducer.register to declare a new entity-set, providing a entity source function and a propertyname to serve as the key
    final InMemoryProducer producer = new InMemoryProducer("InMemoryProducerExample");

    // expose this jvm's thread information (Thread instances) as an entity-set called "Threads"
    producer.register(Thread.class, Long.class, "Threads", new Func<Iterable<Thread>>() {
      public Iterable<Thread> apply() {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        while (tg.getParent() != null)
          tg = tg.getParent();
        Thread[] threads = new Thread[1000];
        int count = tg.enumerate(threads, true);
        return Enumerable.create(threads).take(count);
      }
    }, "Id");

    // expose current system properties (Map.Entry instances) as an entity-set called "SystemProperties"
    producer.register(Entry.class, String.class, "SystemProperties", new Func<Iterable<Entry>>() {
      public Iterable<Entry> apply() {
        return (Iterable<Entry>) (Object) System.getProperties().entrySet();
      }
    }, "Key");

    // expose current environment variables (Map.Entry instances) as an entity-set called "EnvironmentVariables"
    producer.register(Entry.class, String.class, "EnvironmentVariables", new Func<Iterable<Entry>>() {
      public Iterable<Entry> apply() {
        return (Iterable<Entry>) (Object) System.getenv().entrySet();
      }
    }, "Key");

    // expose this producer's entity-types (EdmEntityType instances) as an entity-set called "EdmEntityTypes"
    producer.register(EdmEntityType.class, String.class, "EdmEntityTypes", new Func<Iterable<EdmEntityType>>() {
      public Iterable<EdmEntityType> apply() {
        return producer.getMetadata().getEntityTypes();
      }
    }, "FullyQualifiedTypeName");

    // expose a current listing of exchange traded funds sourced from an external csv (EtfInfo instances) as an entity-set called "ETFs"
    producer.register(EtfInfo.class, String.class, "ETFs", Funcs.wrap(new ThrowingFunc<Iterable<EtfInfo>>() {
      public Iterable<EtfInfo> apply() throws Exception {
        return getETFs();
      }
    }), "Symbol");

    // expose an large list of integers as an entity-set called "Integers"
    producer.register(Integer.class, Integer.class, "Integers", new Func<Iterable<Integer>>() {
      public Iterable<Integer> apply() {
        return Enumerable.range(0, Integer.MAX_VALUE);
      }
    }, Funcs.method(Integer.class, Integer.class, "intValue"));

    // register the producer as the static instance, then launch the http server
    ODataProducerProvider.setInstance(producer);
    ProducerUtil.hostODataServer(endpointUri);
  }

  private static Iterable<EtfInfo> getETFs() throws Exception {
    return Enumerables.lines(new URL("http://www.masterdata.com/HelpFiles/ETF_List_Downloads/AllETFs.csv")).select(new Func1<String, EtfInfo>() {
      public EtfInfo apply(String csvLine) {
        return EtfInfo.parse(csvLine);
      }
    }).skip(1); // skip header line
  }

  public static class EtfInfo {

    private final String name;
    private final String symbol;
    private final String fundType;

    private EtfInfo(String name, String symbol, String fundType) {
      this.name = name;
      this.symbol = symbol;
      this.fundType = fundType;
    }

    public static EtfInfo parse(String csvLine) {

      csvLine = csvLine.substring(0, csvLine.lastIndexOf(','));
      int i = csvLine.lastIndexOf(',');
      String type = csvLine.substring(i + 1);
      csvLine = csvLine.substring(0, csvLine.lastIndexOf(','));
      i = csvLine.lastIndexOf(',');
      String sym = csvLine.substring(i + 1);
      csvLine = csvLine.substring(0, csvLine.lastIndexOf(','));
      String name = csvLine;
      name = name.startsWith("\"") ? name.substring(1) : name;
      name = name.endsWith("\"") ? name.substring(0, name.length() - 1) : name;
      name = name.replace("\u00A0", " ");

      return new EtfInfo(name, sym, type);
    }

    public String getName() {
      return name;
    }

    public String getSymbol() {
      return symbol;
    }

    public String getFundType() {
      return fundType;
    }
  }
}
