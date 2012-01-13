package org.odata4j.jersey.examples.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.behaviors.OClientBehavior;
import org.odata4j.consumer.behaviors.OClientBehaviors;
import org.odata4j.examples.consumers.AbstractDataMarketConsumerExample;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class DataMarketJerseyConsumerExample extends AbstractDataMarketConsumerExample {

  public static void main(String[] args) {
    DataMarketJerseyConsumerExample example = new DataMarketJerseyConsumerExample();
    example.run(args);
  }

  @Override
  public ODataConsumer create(String endpointUri, FormatType formatType, String methodToTunnel) {
    OClientBehavior basicAuth = OClientBehaviors.basicAuth("accountKey", this.getLoginPassword());
    return ODataJerseyConsumer.newBuilder(endpointUri).setClientBehaviors(basicAuth).build();
  }

}
