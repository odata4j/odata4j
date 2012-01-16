package org.odata4j.examples.consumers;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.AbstractExample;
import org.odata4j.examples.ODataEndpoints;

public class TwitPicConsumerExample extends AbstractExample {

  public static void main(String[] args) {
    TwitPicConsumerExample example = new TwitPicConsumerExample();
    example.run(args);
  }

  private void run(String[] args) {
    ODataConsumer c = this.runtime.create(ODataEndpoints.TWITPIC);

    String tag = "starbucks";
    reportEntities("images tagged '" + tag + "'",
        c.getEntities("Tags")
            .nav(tag, "Images")
            .orderBy("Views desc")
            .top(5)
            .execute());

  }

}
