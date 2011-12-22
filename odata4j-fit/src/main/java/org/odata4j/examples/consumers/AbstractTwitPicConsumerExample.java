package org.odata4j.examples.consumers;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.BaseExample;
import org.odata4j.examples.ConsumerExample;
import org.odata4j.examples.ODataEndpoints;

public abstract class AbstractTwitPicConsumerExample extends BaseExample  implements ConsumerExample {

  @Override
  public void run(String... args) {
    ODataConsumer c = this.create(ODataEndpoints.TWITPIC);

    String tag = "starbucks";
    reportEntities("images tagged '" + tag + "'",
        c.getEntities("Tags")
            .nav(tag, "Images")
            .orderBy("Views desc")
            .top(5)
            .execute());

  }

}
