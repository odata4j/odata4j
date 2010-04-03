package org.odata4j.examples.producer;

import java.io.File;
import java.net.URL;
import java.util.Map.Entry;

import org.odata4j.edm.EdmEntityType;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.ODataProducerProvider;

import core4j.Enumerable;
import core4j.Enumerables;
import core4j.Func;
import core4j.Func1;
import core4j.Funcs;
import core4j.ThrowingFunc;

public class InMemoryProducerExample {

    @SuppressWarnings("unchecked")
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
            }}, "Id");

        // expose all files in the current directory (File instances) as an entity-set called "Files"
        producer.register(File.class, String.class, "Files", new Func<Iterable<File>>() {
            public Iterable<File> apply() {
                return Enumerable.create(new File(".").listFiles());
            }}, "Name");

        // expose current system properties (Map.Entry instances) as an entity-set called "SystemProperties"
        producer.register(Entry.class, String.class, "SystemProperties", new Func<Iterable<Entry>>() {
            public Iterable<Entry> apply() {
                return (Iterable<Entry>) (Object) System.getProperties().entrySet();
            }}, "Key");

        // expose current environment variables (Map.Entry instances) as an entity-set called "EnvironmentVariables"
        producer.register(Entry.class, String.class, "EnvironmentVariables", new Func<Iterable<Entry>>() {
            public Iterable<Entry> apply() {
                return (Iterable<Entry>) (Object) System.getenv().entrySet();
            }}, "Key");

        // expose this producer's entity-types (EdmEntityType instances) as an entity-set called "EdmEntityTypes"
        producer.register(EdmEntityType.class, String.class, "EdmEntityTypes", new Func<Iterable<EdmEntityType>>() {
            public Iterable<EdmEntityType> apply() {
                return producer.getMetadata().getEntityTypes();
            }}, "FQName");

        // expose a current listing of exchange traded funds sourced from an external csv (EtfInfo instances) as an entity-set called "ETFs"
        producer.register(EtfInfo.class, String.class, "ETFs", Funcs.wrap(new ThrowingFunc<Iterable<EtfInfo>>() {
            public Iterable<EtfInfo> apply() throws Exception {
                return getETFs();
            }}), "Symbol");

        // register the producer as the static instance, then launch the http server
        ODataProducerProvider.setInstance(producer);
        ExampleUtil.hostODataServer(endpointUri);
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
