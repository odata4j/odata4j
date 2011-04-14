package org.odata4j.producer.jpa.oneoff03;

import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.producer.jpa.oneoff.OneoffTestBase;

public class Oneoff03_ManyToMany extends OneoffTestBase {


	@BeforeClass
	public static void setUpClass() throws Exception {
		setUpClass(Oneoff03_ManyToMany.class,20);		
	}
	
	@Test
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
			
		Assert.assertEquals(1, consumer.getEntities("Student").execute().count());
		Assert.assertEquals(2, consumer.getEntities("Course").execute().count());
		
		OEntity student1 =  consumer.getEntities("Student").execute().first();
		Assert.assertEquals(2,consumer.getEntities(student1.getLink("courses", ORelatedEntitiesLink.class)).execute().count());
		Assert.assertEquals(student1.getEntityKey(), student.getEntityKey());
		for(OEntity course : consumer.getEntities("Course").execute()){
			List<OEntity> courseStudents = consumer.getEntities(course.getLink("students", ORelatedEntitiesLink.class)).execute().toList();
			Assert.assertEquals(1, courseStudents.size());
			
		}
	}
	
}
