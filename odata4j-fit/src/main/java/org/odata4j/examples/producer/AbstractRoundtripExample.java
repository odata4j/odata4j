package org.odata4j.examples.producer;

import java.util.ArrayList;
import java.util.List;

import org.core4j.Func;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.AbstractExample;
import org.odata4j.examples.ConsumerSupport;
import org.odata4j.examples.ProducerSupport;
import org.odata4j.examples.RunSupport;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.server.ODataServer;

public abstract class AbstractRoundtripExample extends AbstractExample implements ConsumerSupport, ProducerSupport, RunSupport {

  public class Customer {

    private int id;
    private String name;

    public Customer() {}

    public Customer(int id, String name) {
      this.id = id;
      this.name = name;
    }

    public int getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public void setId(int id) {
      this.id = id;
    }

    public void setName(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return String.format("Customer[Id=%s,Name=%s]", id, name);
    }

  }

  public void run(String[] args) {

    // create/start the server
    String endpointUri = "http://localhost:8885/RoundtripExample.svc/";

    InMemoryProducer producer = new InMemoryProducer("RoundtripExample");

    producer.register(Customer.class, "Customers", new Func<Iterable<Customer>>() {
      public Iterable<Customer> apply() {
        List<Customer> customers = new ArrayList<Customer>();
        customers.add(new Customer(1, "John"));
        return customers;
      }
    }, "Id");

    DefaultODataProducerProvider.setInstance(producer);
    ODataServer server = this.startODataServer(endpointUri);

    try {
      // create the client
      ODataConsumer.dump.responseHeaders(true);
      ODataConsumer consumer = this.create(endpointUri, null);

      reportEntities("Customers", consumer.getEntities("Customers").execute());

      for (Customer customer : consumer.getEntities(Customer.class, "Customers").execute()) {
        report(customer.toString());
      }

    } finally {
      // stop the server
      server.stop();
    }
  }

}
