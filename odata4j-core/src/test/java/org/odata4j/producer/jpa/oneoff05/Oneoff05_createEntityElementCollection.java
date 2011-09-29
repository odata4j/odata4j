package org.odata4j.producer.jpa.oneoff05;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OProperties;
import org.odata4j.producer.jpa.oneoff.OneoffTestBase;

public class Oneoff05_createEntityElementCollection extends OneoffTestBase {

  @BeforeClass
  public static void setUpClass() throws Exception {
    setUpClass(Oneoff05_createEntityElementCollection.class, 20);
  }

  @Ignore
  @Test
  public void createEntityElementCollection() {
    final long now = System.currentTimeMillis();
    ODataConsumer consumer = ODataConsumer.create(endpointUri);

    consumer
        .createEntity("Student")
        .properties(OProperties.string("StudentName", "Student1" + now))
        //todo add courses as well to the student
        .execute();
  }
}
