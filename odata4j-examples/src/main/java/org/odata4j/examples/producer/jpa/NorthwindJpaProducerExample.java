package org.odata4j.examples.producer.jpa;

import static org.odata4j.examples.JaxRsImplementation.JERSEY;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.odata4j.core.OExtension;
import org.odata4j.examples.AbstractExample;
import org.odata4j.examples.ODataServerFactory;
import org.odata4j.producer.ErrorResponseExtension;
import org.odata4j.producer.ErrorResponseExtensions;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.jpa.JPAProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;

public class NorthwindJpaProducerExample extends AbstractExample {

  public static void main(String[] args) {
    NorthwindJpaProducerExample example = new NorthwindJpaProducerExample();
    example.run(args);
  }

  private void run(String[] args) {

    String endpointUri = "http://localhost:8886/NorthwindJpaProducerExample.svc/";

    // this example assumes you have an appropriate persistence.xml containing a valid persistence unit definition 
    // (in this case named NorthwindServiceEclipseLink) mapping your jpa entity classes, etc

    // create a JPAProducer by giving it a EntityManagerFactory
    String persistenceUnitName = "NorthwindService" + JPAProvider.JPA_PROVIDER.caption;
    String namespace = "Northwind";
    EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName);

    JPAProducer producer = new JPAProducer(emf, namespace, 50) {
      @Override
      public <TExtension extends OExtension<ODataProducer>> TExtension findExtension(Class<TExtension> clazz, Map<String, Object> params) {
        if (clazz.equals(ErrorResponseExtension.class))
          return clazz.cast(ErrorResponseExtensions.ALWAYS_RETURN_INNER_ERRORS);
        return null;
      }
    };
    DatabaseUtils.fillDatabase(namespace.toLowerCase(), "/META-INF/northwind_insert.sql");

    // register the producer as the static instance, then launch the http server
    DefaultODataProducerProvider.setInstance(producer);
    new ODataServerFactory(JERSEY).hostODataServer(endpointUri);

  }

}
