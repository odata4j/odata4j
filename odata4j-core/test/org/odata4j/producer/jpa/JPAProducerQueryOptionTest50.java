package org.odata4j.producer.jpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.examples.producer.ProducerUtil;
import org.odata4j.producer.resources.ODataProducerProvider;
import org.odata4j.producer.server.JerseyServer;

public class JPAProducerQueryOptionTest50 {

	private static final String endpointUri =
			"http://localhost:8810/northwind/Northwind.svc/";
	private static EntityManagerFactory emf;
	private static JerseyServer server;

	@BeforeClass
	public static void setUpClass() throws Exception {
		String persistenceUnitName = "NorthwindService";
		String namespace = "Northwind";

		emf = Persistence.createEntityManagerFactory(
				persistenceUnitName);

		JPAProducer producer = new JPAProducer(
				emf,
				namespace,
				50);

		NorthwindTestUtils.fillDatabase(emf);

		ODataProducerProvider.setInstance(producer);
		server = ProducerUtil.startODataServer(endpointUri);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		if (server != null) {
			server.stop();
		}

		if (emf != null) {
			emf.close();
		}
	}

	@Test
	public void SystemQueryOptionFilterNotEqualTest() {
		String inp = "SystemQueryOptionFilterNotEqualTest";
		String uri = "Suppliers?$filter=Country ne 'UK'";
		NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
	}

	@Test
	public void ResourcePathComplexFilterEqualTest() {
		String inp = "ResourcePathComplexFilterEqualTest";
		String uri = "Categories(1)/Products?$filter=Supplier/Address eq '49 Gilbert St.'";
		NorthwindTestUtils.TestJSONResult(endpointUri, uri, inp);
	}
}
