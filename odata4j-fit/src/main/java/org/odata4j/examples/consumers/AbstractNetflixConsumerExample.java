package org.odata4j.examples.consumers;

import java.util.List;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.examples.AbstractExample;
import org.odata4j.examples.ODataEndpoints;
import org.odata4j.fit.support.ConsumerSupport;
import org.odata4j.fit.support.RunSupport;

public abstract class AbstractNetflixConsumerExample extends AbstractExample implements ConsumerSupport, RunSupport {

  @Override
  public void run(String[] args) {

    ODataConsumer c = this.create(ODataEndpoints.NETFLIX, null, null);

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
