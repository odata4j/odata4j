package org.odata4j.producer.jpa.northwind.test;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;



public class IssuesFailingTest extends JPAProducerTestBase{

	protected static final String endpointUri = "http://localhost:8810/northwind/Northwind.svc/";

	@BeforeClass
	public static void setUpClass() throws Exception {
		
		setUpClass(20);		
	}
		
	//@Test
	public void createManyToMany()
	{
		final long now = System.currentTimeMillis();
		ODataConsumer consumer = ODataConsumer.create(endpointUri);
		
		OEntity course1 = consumer
		.createEntity("Course")
		.properties(OProperties.string("CourseName", "Name1" + now))
		.get();
		
		OEntity course2 = consumer
		.createEntity("Course")
		.properties(OProperties.string("CourseName", "Name2" + now))
		.get();
		
		OEntity student = consumer
		.createEntity("Student")
		.properties(OProperties.string("StudentName", "Student" + now))
		.inline("courses", course1, course2)
		.execute();
		
		Assert.assertNotNull(student);	
	}
	

}
