package org.odata4j.jersey.examples.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.behaviors.OClientBehavior;
import org.odata4j.consumer.behaviors.OClientBehaviors;
import org.odata4j.examples.consumers.AbstractDallasConsumerExampleUnescoUIS;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class DallasJerseyConsumerExampleUnescoUIS extends AbstractDallasConsumerExampleUnescoUIS{

  public static void main(String[] args) {
    DallasJerseyConsumerExampleUnescoUIS example = new DallasJerseyConsumerExampleUnescoUIS();
    example.run(args);
  }
  
  @Override
  public ODataConsumer create(String endpointUri) {
    OClientBehavior basicAuth = OClientBehaviors.basicAuth("accountKey", this.getLoginPassword());
    return ODataJerseyConsumer.newBuilder(endpointUri).setClientBehaviors(basicAuth).build();
  }

}
