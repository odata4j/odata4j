package org.odata4j.producer.jpa.oneoff05;

import org.junit.Ignore;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OProperties;
import org.odata4j.producer.jpa.oneoff.AbstractOneoffTestBase;

public class Oneoff05_createEntityElementCollection extends AbstractOneoffTestBase {

  public Oneoff05_createEntityElementCollection(RuntimeFacadeType type) {
    super(type);
  }

  @Ignore
  @Test
  public void createEntityElementCollection() {
    final long now = System.currentTimeMillis();
    ODataConsumer consumer = this.rtFacade.create(endpointUri, null, null);

    consumer
        .createEntity("Student")
        .properties(OProperties.string("StudentName", "Student1" + now))
        //todo add courses as well to the student
        .execute();
  }
}
