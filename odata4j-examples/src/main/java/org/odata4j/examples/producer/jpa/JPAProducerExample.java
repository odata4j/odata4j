package org.odata4j.examples.producer.jpa;

import static org.odata4j.examples.JaxRsImplementation.JERSEY;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.odata4j.examples.AbstractExample;
import org.odata4j.examples.ODataServerFactory;
import org.odata4j.examples.producer.jpa.northwind.NorthwindUtils;
import org.odata4j.producer.jpa.JPAProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;

public class JPAProducerExample extends AbstractExample {

  public static void main(String[] args) {
    JPAProducerExample example = new JPAProducerExample();
    example.run(args);
  }

  private void run(String[] args) {

    String endpointUri = "http://localhost:8886/JPAProducerExample.svc/";

    // this example assumes you have an appropriate persistence.xml containing a valid persistence unit definition 
    // (in this case named NorthwindServiceEclipseLink) mapping your jpa entity classes, etc

    // create a JPAProducer by giving it a EntityManagerFactory
    String persistenceUnitName = "NorthwindService" + JPAProvider.JPA_PROVIDER.caption;
    String namespace = "Northwind";
    EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName);

    JPAProducer producer = new JPAProducer(emf, namespace, 50);
    NorthwindUtils.fillDatabase(emf);

    // register the producer as the static instance, then launch the http server
    DefaultODataProducerProvider.setInstance(producer);
    new ODataServerFactory(JERSEY).hostODataServer(endpointUri);

  }

}
