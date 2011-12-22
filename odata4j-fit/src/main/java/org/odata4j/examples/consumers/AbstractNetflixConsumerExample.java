package org.odata4j.examples.consumers;

import java.util.List;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.examples.BaseExample;
import org.odata4j.examples.ConsumerExample;
import org.odata4j.examples.ODataEndpoints;

public abstract class AbstractNetflixConsumerExample extends BaseExample implements ConsumerExample {

  @Override
  public void run(String... args) {

    ODataConsumer c = this.create(ODataEndpoints.NETFLIX);

    // locate the netflix id for Morgan Spurlock
    int morganSpurlockId = c.getEntities("People").filter("substringof('Spurlock',Name)").execute().first().getProperty("Id", Integer.class).getValue();

    // lookup and print all titles he's acted in
    List<OEntity> titlesActedIn = c.getEntities("People").nav(morganSpurlockId, "TitlesActedIn").execute().toList();
    for (OEntity title : titlesActedIn) {
      for (OProperty<?> p : title.getProperties()) {
        report("%s: %s", p.getName(), p.getValue());
      }
      report("\n");
    }
    report("count: " + titlesActedIn.size());

  }

}
