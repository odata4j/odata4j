package org.odata4j.examples.consumers;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.examples.AbstractCredentialsExample;
import org.odata4j.examples.ConsumerSupport;
import org.odata4j.examples.RunSupport;

public abstract class AbstractDataMarketConsumerExample extends AbstractCredentialsExample implements ConsumerSupport, RunSupport {

  @Override
  public void run(String[] args) {

    String[] datamarketCreds = args.length > 0 ? args : System.getenv("DATAMARKET").split(":");
    this.setLoginPassword(datamarketCreds[0]);
    
    String url = "https://api.datamarket.azure.com/Data.ashx/UnitedNations/MDG/";

    ODataConsumer c = this.create(url);

    OEntity firstDataSeries = c.getEntities("DataSeries").top(1).execute().first();
    String filter = String.format("DataSeriesId eq '%s'", firstDataSeries.getProperty("Id").getValue());
    reportEntities(firstDataSeries.getProperty("Name", String.class).getValue(), c.getEntities("Values").filter(filter).top(10).execute());

  }

}
