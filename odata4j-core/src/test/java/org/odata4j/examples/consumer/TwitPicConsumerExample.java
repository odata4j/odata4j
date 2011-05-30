package org.odata4j.examples.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.BaseExample;
import org.odata4j.examples.ODataEndpoints;

public class TwitPicConsumerExample extends BaseExample {

  public static void main(String[] args) {

    ODataConsumer c = ODataConsumer.create(ODataEndpoints.TWITPIC);

    String tag = "starbucks";
    reportEntities("images tagged '" + tag + "'",
        c.getEntities("Tags")
        .nav(tag, "Images")
        .orderBy("Views desc")
        .top(5)
        .execute());

  }

}
