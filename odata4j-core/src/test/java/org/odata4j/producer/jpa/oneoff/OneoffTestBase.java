package org.odata4j.producer.jpa.oneoff;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.odata4j.examples.producer.ProducerUtil;
import org.odata4j.producer.jpa.JPAProducer;
import org.odata4j.producer.resources.ODataProducerProvider;
import org.odata4j.producer.server.JerseyServer;

public class OneoffTestBase {
	
	protected static String endpointUri;
	
	protected static EntityManagerFactory emf;
	protected static JerseyServer server;

	@AfterClass
	public static void tearDownClass() throws Exception {
		if (server != null) {
			server.stop();
		}

		if (emf != null) {
			emf.close();
		}
	}
	
	public static void setUpClass(Class<?> testClass, int maxResults) throws Exception {
		
		String name = testClass.getSimpleName().split("_")[0];
		
		endpointUri = "http://localhost:8810/"+name+".svc/";
		String persistenceUnitName = name;
		String namespace = name;
	
		Map<String,String> p = new HashMap<String,String>();
		p.put("eclipselink.target-database", "HSQL");
		p.put("eclipselink.jdbc.driver", "org.hsqldb.jdbcDriver");
		p.put("eclipselink.jdbc.url", "jdbc:hsqldb:mem:"+name+";shutdown=true;ifxeists=true");
		p.put("eclipselink.jdbc.user", "sa");
		p.put("eclipselink.jdbc.password", "");
		p.put("eclipselink.ddl-generation", "create-tables");
		p.put("eclipselink.logging.level", "SEVERE");
		p.put("eclipselink.logging.exceptions", "true");
		
		emf = Persistence.createEntityManagerFactory(persistenceUnitName,p);
	
		JPAProducer producer = new JPAProducer(
				emf,
				namespace,
				maxResults); 
	
		
	
		ODataProducerProvider.setInstance(producer);
		server = ProducerUtil.startODataServer(endpointUri);
	}
	
	
	
	
}
