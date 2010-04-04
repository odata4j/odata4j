package org.odata4j.examples.producer;

import javax.persistence.Persistence;

import org.odata4j.producer.jpa.JPAProducer;
import org.odata4j.producer.resources.ODataProducerProvider;

public class JPAProducerExample {

    public static void main(String[] args) {
        
        String endpointUri = "http://localhost:8886/JPAProducerExample.svc/";
        
        // this example assumes you have an appropriate persistence.xml containing a valid persistence unit definition 
        // (in this case named NorthwindService) mapping your jpa entity classes, etc
        
        // create a JPAProducer by giving it a EntityManagerFactory
        String persistenceUnitName = "NorthwindService";
        String namespace = "Northwind";
        JPAProducer producer = new JPAProducer(Persistence.createEntityManagerFactory(persistenceUnitName), namespace);

        // register the producer as the static instance, then launch the http server
        ODataProducerProvider.setInstance(producer);
        ProducerUtil.hostODataServer(endpointUri);

    }

}
