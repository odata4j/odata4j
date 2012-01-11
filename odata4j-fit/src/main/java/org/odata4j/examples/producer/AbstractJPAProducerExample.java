package org.odata4j.examples.producer;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.odata4j.examples.ProducerSupport;
import org.odata4j.examples.RunSupport;
import org.odata4j.producer.jpa.JPAProducer;
import org.odata4j.producer.jpa.northwind.test.NorthwindTestDataUtils;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.test.JPAProvider;

public abstract class AbstractJPAProducerExample implements ProducerSupport, RunSupport {

  public void run(String[] args) {

    String endpointUri = "http://localhost:8886/JPAProducerExample.svc/";

    // this example assumes you have an appropriate persistence.xml containing a valid persistence unit definition 
    // (in this case named NorthwindServiceEclipseLink) mapping your jpa entity classes, etc

    // create a JPAProducer by giving it a EntityManagerFactory
    String persistenceUnitName = "NorthwindService" + JPAProvider.JPA_PROVIDER.caption;
    String namespace = "Northwind";
    EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName);

    JPAProducer producer = new JPAProducer(emf, namespace, 50);
    NorthwindTestDataUtils.fillDatabase(emf);

    // register the producer as the static instance, then launch the http server
    DefaultODataProducerProvider.setInstance(producer);
    this.hostODataServer(endpointUri);

  }

}
