package org.odata4j.producer.jpa.oneoff02;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.producer.jpa.oneoff.OneoffTestBase;

public class Oneoff02_ManyToMany extends OneoffTestBase {


	@BeforeClass
	public static void setUpClass() throws Exception {
		setUpClass(Oneoff02_ManyToMany.class,20);		
	}
	
	@Test
	public void createManyToMany()
	{
		ODataConsumer.dump.all(true);
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
