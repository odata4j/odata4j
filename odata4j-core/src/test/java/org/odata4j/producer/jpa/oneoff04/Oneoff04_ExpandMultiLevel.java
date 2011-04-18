package org.odata4j.producer.jpa.oneoff04;

import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.producer.jpa.northwind.test.NorthwindTestUtils;
import org.odata4j.producer.jpa.oneoff.OneoffTestBase;

public class Oneoff04_ExpandMultiLevel extends OneoffTestBase {

	@BeforeClass
	public static void setUpClass() throws Exception {
		setUpClass(Oneoff04_ExpandMultiLevel.class,20);		
	}
	
	@Ignore
	@Test
	public void ExpandMultiLevel()
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
		
		OEntity student1 = consumer
			.createEntity("Student")
			.properties(OProperties.string("StudentName", "Student1" + now))
			.inline("courses", course1, course2)
			.get();
		
		OEntity student2 = consumer
			.createEntity("Student")
			.properties(OProperties.string("StudentName", "Student2" + now))
			.inline("courses", course1, course2)
			.get();
		
		OEntity school=consumer
			.createEntity("School")
			.properties(OProperties.string("SchoolName", "School" + now))
			.inline("students",student1,student2)
			.execute();
			
		Assert.assertEquals(1, consumer.getEntities("School").execute().count());
		Assert.assertEquals(2, consumer.getEntities("Student").execute().count());
		Assert.assertEquals(2, consumer.getEntities("Course").execute().count());
		
		OEntity school1 =  consumer.getEntities("School").expand("students/courses").execute().first();
		
		
		Assert.assertNotNull(school1);
		
		
		
	}
}
