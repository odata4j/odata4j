package org.odata4j.examples.consumers;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.AbstractExample;
import org.odata4j.examples.ODataEndpoints;
import org.odata4j.fit.support.ConsumerSupport;
import org.odata4j.fit.support.RunSupport;

public abstract class AbstractTwitPicConsumerExample extends AbstractExample implements ConsumerSupport, RunSupport {

  @Override
  public void run(String[] args) {
    ODataConsumer c = this.create(ODataEndpoints.TWITPIC, null, null);

    String tag = "starbucks";
    reportEntities("images tagged '" + tag + "'",
        c.getEntities("Tags")
            .nav(tag, "Images")
            .orderBy("Views desc")
            .top(5)
            .execute());

  }

}
