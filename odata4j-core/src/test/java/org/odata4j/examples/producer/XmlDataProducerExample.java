package org.odata4j.examples.producer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.core4j.Enumerable;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmType;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.Responses;
import org.odata4j.producer.exceptions.NotImplementedException;
import org.odata4j.producer.jpa.northwind.Customers;
import org.odata4j.producer.jpa.northwind.test.NorthwindTestUtils;
import org.odata4j.producer.resources.ODataProducerProvider;

/**
 * This example shows how to expose xml data as atom feed.
 * 
 */
public class XmlDataProducerExample {

	public static final String endpointUri = "http://localhost:8010/XmlDataProducerExample.svc";

	public static void main(String[] args) throws Exception {

		System.out.println("Please direct your browerser to " + endpointUri
				+ "Customers");

		// register the producer as the static instance, then launch the http
		// server
		ODataProducerProvider.setInstance(new XmlDataProducer());
		ProducerUtil.hostODataServer(endpointUri);

		// generateXmlTestData();
	}

	@XmlRootElement
	public static class CustomersList {
		@XmlElement
		Customers[] customers;
	}

	public static void generateXmlTestData() throws Exception {
		EntityManagerFactory emf;
		String persistenceUnitName = "NorthwindServiceEclipseLink";

		// create an fill temporary database
		emf = Persistence.createEntityManagerFactory(persistenceUnitName);
		emf.createEntityManager().close();
		NorthwindTestUtils.fillDatabase(emf);

		// select the customers
		EntityManager em = emf.createEntityManager();
		try {
			Query q = em.createQuery("SELECT c FROM Customers c");

			// marshal them to the test data file
			Marshaller marshaller = JAXBContext
					.newInstance(CustomersList.class)
					.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);

			Writer out = new OutputStreamWriter(new FileOutputStream(
					"xmlDataProducerExampleTestData.xml"), "utf-8");
			try {
				List<?> res = q.getResultList();
				CustomersList c = new CustomersList();
				c.customers = res.toArray(new Customers[res.size()]);
				marshaller.marshal(c, out);
				out.flush();
			} finally {
				out.close();
			}
		} finally {
			em.close();
		}
	}

	/**
	 * Sample ODataProducer for providing xml data as atom feed.
	 */
	public static class XmlDataProducer implements ODataProducer {

		private final EdmDataServices metadata;
		private XMLInputFactory xmlInputFactory;

		public XmlDataProducer() {
			// build the metadata here hardcoded as example
			// one would probably generate it from xsd schema or something else
			String namespace = "XmlExample";

			List<EdmProperty> properties = new ArrayList<EdmProperty>();
			properties.add(new EdmProperty("address", EdmType.STRING, false));
			properties.add(new EdmProperty("city", EdmType.STRING, false));
			properties
					.add(new EdmProperty("companyName", EdmType.STRING, false));
			properties
					.add(new EdmProperty("contactName", EdmType.STRING, false));
			properties.add(new EdmProperty("contactTitle", EdmType.STRING,
					false));
			properties.add(new EdmProperty("country", EdmType.STRING, false));
			properties
					.add(new EdmProperty("customerID", EdmType.STRING, false));
			properties.add(new EdmProperty("fax", EdmType.STRING, false));
			properties.add(new EdmProperty("phone", EdmType.STRING, false));
			properties
					.add(new EdmProperty("postalCode", EdmType.STRING, false));

			List<EdmEntityType> entityTypes = new ArrayList<EdmEntityType>();
			EdmEntityType type = new EdmEntityType(namespace, null,
					"Customers", null, Arrays.asList("customerID"), properties,
					null);
			entityTypes.add(type);

			List<EdmEntitySet> entitySets = new ArrayList<EdmEntitySet>();
			entitySets.add(new EdmEntitySet("Customers", type));

			EdmEntityContainer container = new EdmEntityContainer(namespace
					+ "Entities", true, null, entitySets, null, null);
			EdmSchema modelSchema = new EdmSchema(namespace + "Model", null,
					entityTypes, null, null, null);
			EdmSchema containerSchema = new EdmSchema(namespace + "Container",
					null, null, null, null, Enumerable.create(container)
							.toList());

			metadata = new EdmDataServices(ODataConstants.DATA_SERVICE_VERSION,
					Enumerable.create(modelSchema, containerSchema).toList());

			xmlInputFactory = XMLInputFactory.newInstance();
		}

		@Override
		public EdmDataServices getMetadata() throws Exception{
			return this.metadata;
		}

		/**
		 * Returns OEntities build from xml data. In the real world the xml data
		 * could be filtered using the provided <code>queryInfo.filter</code>.
		 * The real implementation should also respect
		 * <code>queryInfo.top</code> and <code>queryInfo.skip</code>.
		 */
		@Override
		public EntitiesResponse getEntities(String entitySetName,
				QueryInfo queryInfo) throws Exception{
			EdmEntitySet ees = getMetadata().getEdmEntitySet(entitySetName);
			
			InputStream is = getClass()
				.getResourceAsStream("xmlDataProducerExampleTestData.xml");
			XMLEventReader reader = null;
			try {
				// transform the xml to OEntities with OProperties.
				// links are omitted for simplicity
				reader = xmlInputFactory .createXMLEventReader(is);
				
				List<OEntity> entities = new ArrayList<OEntity>();
				List<OProperty<?>> properties = new ArrayList<OProperty<?>>();
				boolean inCustomer = false;
				String id = null;
				String data = null;
				while (reader.hasNext()) {
					XMLEvent event = reader.nextEvent();

					if (event.isStartElement()) {
						if ("customers".equals(event.asStartElement().getName()
								.getLocalPart())) {
							inCustomer = true;
						}
					} else if (event.isEndElement()) {
						String name = event.asEndElement().getName()
								.getLocalPart();
						if ("customers".equals(name)) {
							entities.add(OEntities.create(ees, OEntityKey.create(id), properties, null));
							properties = new ArrayList<OProperty<?>>();
							inCustomer = false;
						} else if (inCustomer) {
							if ("customerID".equals(name)) {
								id = data;
							}
							properties.add(OProperties.string(name, data));
						}
					} else if (event.isCharacters()) {
						data = event.asCharacters().getData();
					}
				}

				return Responses.entities(entities, ees, null, null);
			} catch (XMLStreamException ex) {
				throw new RuntimeException(ex);
			} finally {
				try {
					if (reader != null)
						reader.close();
				} catch (XMLStreamException ignore) {
				}
				
				try {
					is.close();
				} catch (IOException ignore) {
				}
			}
		}
		
		@Override
		public EntitiesResponse getNavProperty(String entitySetName,
				OEntityKey entityKey, String navProp, QueryInfo queryInfo) throws Exception{
			 throw new NotImplementedException();
		}

		@Override
		public void close() {
		}

		@Override
		public EntityResponse createEntity(String entitySetName, OEntity entity) throws Exception{
			 throw new NotImplementedException();
		}

		@Override
	    public EntityResponse createEntity(String entitySetName, OEntityKey entityKey, String navProp, OEntity entity) throws Exception{
			 throw new NotImplementedException();
		}

		@Override
		public void deleteEntity(String entitySetName, OEntityKey entityKey) throws Exception{
			 throw new NotImplementedException();
		}

		@Override
		public void mergeEntity(String entitySetName, OEntity entity) throws Exception{
			 throw new NotImplementedException();
		}

		@Override
		public void updateEntity(String entitySetName, OEntity entity) throws Exception{
			 throw new NotImplementedException();
		}

		@Override
		public EntityResponse getEntity(String entitySetName,
				OEntityKey entityKey, QueryInfo queryInfo) throws Exception {
			 throw new NotImplementedException();
		}

	}
}
