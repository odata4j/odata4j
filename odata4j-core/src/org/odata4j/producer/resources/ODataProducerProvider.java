package org.odata4j.producer.resources;

import javax.ws.rs.ext.Provider;

import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.ODataProducerFactory;


import com.sun.jersey.core.impl.provider.xml.LazySingletonContextProvider;

@Provider
public class ODataProducerProvider extends LazySingletonContextProvider<ODataProducer> {

	public static String FACTORY_PROPNAME = "odata4j.producerfactory";
	
	private static ODataProducer STATIC;
	public static void setInstance(ODataProducer producer){
		STATIC = producer;
	}
	
	
	private ODataProducer instance;
	
	public ODataProducerProvider() {
		super(ODataProducer.class);
	}

	@Override
	protected ODataProducer getInstance() {
		if (instance==null){
			if (STATIC!=null){
				instance = STATIC;
			} else {
				instance = loadFromProperties();
			}
		}
		return instance;
	}
	
	private ODataProducer loadFromProperties(){
		try {
			String factoryTypeName = System.getProperty(FACTORY_PROPNAME);
			Class<?> factoryType = Class.forName(factoryTypeName);
			Object obj = factoryType.newInstance();
			ODataProducerFactory factory = (ODataProducerFactory)obj;
			return factory.create(System.getProperties());	// TODO scoped?
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

}
