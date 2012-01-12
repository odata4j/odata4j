package org.odata4j.jersey.examples.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.behaviors.OClientBehaviors;
import org.odata4j.examples.consumers.AbstractCodePlexConsumerExample;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class CodePlexJerseyConsumerExample extends AbstractCodePlexConsumerExample {

  public static void main(String[] args) {
    CodePlexJerseyConsumerExample example = new CodePlexJerseyConsumerExample();
    example.run(args);
  }

  @Override
  public ODataConsumer create(String endpointUri, FormatType formatType, String methodToTunnel) {
    return ODataJerseyConsumer.newBuilder(endpointUri).setClientBehaviors(OClientBehaviors.basicAuth(this.getLoginName(), this.getLoginPassword())).build();
  }

}
