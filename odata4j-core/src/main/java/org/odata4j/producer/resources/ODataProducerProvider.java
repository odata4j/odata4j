package org.odata4j.producer.resources;

import java.util.Properties;
import java.util.logging.Logger;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.ODataProducerFactory;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.impl.provider.xml.LazySingletonContextProvider;

@Provider
public class ODataProducerProvider extends LazySingletonContextProvider<ODataProducer> {

    private final Logger log = Logger.getLogger(getClass().getName());
    public static final String FACTORY_PROPNAME = "odata4j.producerfactory";
    private static ODataProducer STATIC;
    @Context
    private ResourceConfig resourceConfig;

    public static void setInstance(ODataProducer producer) {
        STATIC = producer;
    }
    private ODataProducer instance;

    public ODataProducerProvider() {
        super(ODataProducer.class);
    }

    @Override
    protected ODataProducer getInstance() {
        if (instance == null) {
            if (STATIC != null) {
                log.info("Setting producer instance to static instance: " + STATIC);
                instance = STATIC;
            } else if (resourceConfig != null && resourceConfig.getProperty(FACTORY_PROPNAME) != null) {
                String factoryTypeName = (String) resourceConfig.getProperty(FACTORY_PROPNAME);
                log.info("Creating producer from factory in resource config: " + factoryTypeName);
                Properties p = new Properties();
                p.putAll(resourceConfig.getProperties());
                instance = newProducerFromFactory(factoryTypeName, p);
            } else if (System.getProperty(FACTORY_PROPNAME) != null) {
                String factoryTypeName = System.getProperty(FACTORY_PROPNAME);
                log.info("Creating producer from factory in system properties: " + factoryTypeName);
                instance = newProducerFromFactory(factoryTypeName, System.getProperties());
            } else {
                throw new RuntimeException("Unable to find an OData producer implementation. Call ODataProducerProvider.setInstance to set the static singleton or set the producer factory property \'" + FACTORY_PROPNAME + "\' in either the resource config (web.xml) or system properties to a class name that implements ODataProducerFactory.");
            }
        }
        return instance;
    }

    private ODataProducer newProducerFromFactory(String factoryTypeName, Properties props) {
        try {
            Class<?> factoryType;
            factoryType = Class.forName(factoryTypeName);
            Object obj = factoryType.newInstance();
            ODataProducerFactory factory = (ODataProducerFactory) obj;
            return factory.create(props);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
