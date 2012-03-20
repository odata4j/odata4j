package org.odata4j.examples.consumer;

import static org.odata4j.examples.JaxRsImplementation.JERSEY;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.AbstractExample;
import org.odata4j.examples.ODataConsumerFactory;

public class TwitPicConsumerExample extends AbstractExample {

  public static void main(String[] args) {
    TwitPicConsumerExample example = new TwitPicConsumerExample();
    example.run(args);
  }

  private void run(String[] args) {
    ODataConsumer c = new ODataConsumerFactory(JERSEY).createODataConsumer(ODataEndpoints.TWITPIC, null, null);

    String tag = "starbucks";
    reportEntities("images tagged '" + tag + "'",
        c.getEntities("Tags")
            .nav(tag, "Images")
            .orderBy("Views desc")
            .top(5)
            .execute());

  }

}
