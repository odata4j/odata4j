package org.odata4j.examples.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.ODataConsumers;
import org.odata4j.core.OEntity;
import org.odata4j.examples.BaseExample;

public class DataMarketConsumerExample extends BaseExample {

  public static void main(String[] args) {

    String[] datamarketCreds = args.length > 0 ? args : System.getenv("DATAMARKET").split(":");
    String accountKey = datamarketCreds[0];

    String url = "https://api.datamarket.azure.com/Data.ashx/UnitedNations/MDG/";

    ODataConsumer c = ODataConsumers.dataMarket(url, accountKey);

    OEntity firstDataSeries = c.getEntities("DataSeries").top(1).execute().first();
    String filter = String.format("DataSeriesId eq '%s'", firstDataSeries.getProperty("Id").getValue());
    reportEntities(firstDataSeries.getProperty("Name", String.class).getValue(), c.getEntities("Values").filter(filter).top(10).execute());

  }

}
