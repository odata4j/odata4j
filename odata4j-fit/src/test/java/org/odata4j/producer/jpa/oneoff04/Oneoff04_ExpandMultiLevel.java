package org.odata4j.producer.jpa.oneoff04;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.producer.jpa.oneoff.AbstractOneoffTestBase;

public class Oneoff04_ExpandMultiLevel extends AbstractOneoffTestBase {

  public Oneoff04_ExpandMultiLevel(RuntimeFacadeType type) {
    super(type);
  }

  @Ignore
  @Test
  public void expandMultiLevel() {
    final long now = System.currentTimeMillis();
    ODataConsumer consumer = this.rtFacade.create(endpointUri, null, null);

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

    consumer
        .createEntity("School")
        .properties(OProperties.string("SchoolName", "School" + now))
        .inline("students", student1, student2)
        .execute();

    Assert.assertEquals(1, consumer.getEntities("School").execute().count());
    Assert.assertEquals(2, consumer.getEntities("Student").execute().count());
    Assert.assertEquals(2, consumer.getEntities("Course").execute().count());

    OEntity school1 = consumer.getEntities("School").expand("students/courses").execute().first();

    Assert.assertNotNull(school1);

  }
}
