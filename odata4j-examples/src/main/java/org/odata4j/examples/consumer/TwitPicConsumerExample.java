package org.odata4j.examples.consumer;

import org.odata4j.consumer.ODataClientException;
import org.odata4j.consumer.ODataServerException;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.ODataConsumers;
import org.odata4j.examples.AbstractExample;

public class TwitPicConsumerExample extends AbstractExample {

  public static void main(String[] args) {
    TwitPicConsumerExample example = new TwitPicConsumerExample();
    example.run(args);
  }

  private void run(String[] args) {
    ODataConsumer c = ODataConsumers.create(ODataEndpoints.TWITPIC);
    String tag = "starbucks";

    try {
      reportEntities("images tagged '" + tag + "'",
          c.getEntities("Tags")
              .nav(tag, "Images")
              .orderBy("Views desc")
              .top(5)
              .execute());
    } catch (ODataServerException e) {
      reportError(e);
    } catch (ODataClientException e) {
      report("Client error: " + e.getMessage());
    }

  }

}
