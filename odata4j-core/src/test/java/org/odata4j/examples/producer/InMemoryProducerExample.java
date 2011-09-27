package org.odata4j.examples.producer;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.core4j.Enumerable;
import org.core4j.Enumerables;
import org.core4j.Func;
import org.core4j.Func1;
import org.core4j.Funcs;
import org.core4j.ThrowingFunc;
import org.odata4j.core.IAnnotation;
import org.odata4j.core.Namespace;
import org.odata4j.core.OComplexObjects;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmAnnotation;
import org.odata4j.edm.EdmAnnotationAttribute;
import org.odata4j.edm.EdmAnnotationElement;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDocumentation;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmItem;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmStructuralType;
import org.odata4j.edm.IEdmDecorator;
import org.odata4j.producer.Path;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.ODataProducerProvider;

public class InMemoryProducerExample {

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static void main(String[] args) {

    String endpointUri = "http://localhost:8887/InMemoryProducerExample.svc/";

    // InMemoryProducer is a readonly odata provider that serves up POJOs as entities using bean properties
    // call InMemoryProducer.register to declare a new entity-set, providing a entity source function and a propertyname to serve as the key
    final InMemoryProducer producer = new InMemoryProducer("InMemoryProducerExample", 100, new MyEdmDecorator());

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
  
  public static class MyEdmDecorator implements IEdmDecorator {

    public static final String namespace = "http://tempuri.org";
    public static final String prefix = "inmem";
    
    public MyEdmDecorator() {
      namespaces.add(new Namespace(namespace, prefix));
      this.schemaInfoType = createSchemaInfoType();
    }
    private final List<Namespace> namespaces = new ArrayList<Namespace>(1);
    
    @Override
    public List<Namespace> getNamespaces() {
      return namespaces;
    }

    private final EdmComplexType schemaInfoType;
    
    @Override
    public EdmDocumentation getDocumentationForSchema(String namespace, String typeName) {
      return new EdmDocumentation("InMemoryProducerExample", "This schema exposes a few example types to demonstrate the InMemoryProducer");
    }

    private EdmComplexType createSchemaInfoType() {
        List<EdmProperty> props = new ArrayList<EdmProperty>();

        EdmProperty ep = new EdmProperty(
                "Author",
                EdmSimpleType.STRING, // EdmType type,
                false); // boolean nullable,
        props.add(ep);

        ep = new EdmProperty(
                "SeeAlso",
                EdmSimpleType.STRING, // EdmType type,
                false); // boolean nullable,
        props.add(ep);

        return new EdmComplexType(namespace, "SchemaInfo", props);
      
    }

    @Override
    public List<EdmAnnotation> getAnnotationsForSchema(String namespace, String typeName) {
      List<EdmAnnotation> annots = new ArrayList<EdmAnnotation>();
      annots.add(new EdmAnnotationAttribute(namespace, prefix, "Version", "1.0 early experience pre-alpha"));
      
      List<OProperty<?>> p = new ArrayList<OProperty<?>>();
      p.add(OProperties.string("Author", "Xavier S. Dumont"));
      p.add(OProperties.string("SeeAlso", "InMemoryProducerExample.java"));
      
      annots.add(new EdmAnnotationElement(namespace, prefix, "SchemaInfo", OComplexObjects.create(schemaInfoType, p)));

      return annots;
    }

    @Override
    public EdmDocumentation getDocumentationForEntityType(String namespace, String typeName) {
      return null;
    }

    @Override
    public List<EdmAnnotation> getAnnotationsForEntityType(String namespace, String typeName) {
      return null;
    }

    @Override
    public Object resolveStructuralTypeProperty(EdmStructuralType st, Path path) throws Exception {
      return null;
    }

    @Override
    public EdmDocumentation getDocumentationForProperty(String namespace, String typename, String propName) {
      return null;
    }

    @Override
    public List<EdmAnnotation> getAnnotationsForProperty(String namespace, String typename, String propName) {
      return null;
    }

    @Override
    public Object resolvePropertyProperty(EdmProperty st, Path path) throws Exception {
      return null;
    }

    @Override
    public Object getAnnotationValueOverride(EdmItem item, IAnnotation annot, boolean flatten, Locale locale, Map<String, String> options) {
      return null;
    }

    @Override
    public void decorateEntity(EdmEntitySet entitySet, EdmItem item, EdmItem originalQueryItem, List<OProperty<?>> props, boolean flatten, Locale locale, Map<String, String> options) {
      // no-op
    }
    
  }
}
