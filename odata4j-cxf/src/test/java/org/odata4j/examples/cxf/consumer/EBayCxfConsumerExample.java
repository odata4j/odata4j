package org.odata4j.examples.cxf.consumer;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.cxf.consumer.ODataCxfConsumer;
import org.odata4j.examples.consumers.AbstractEBayConsumerExample;
import org.odata4j.format.FormatType;

public class EBayCxfConsumerExample extends AbstractEBayConsumerExample {

  public static void main(String... args) {
    EBayCxfConsumerExample example = new EBayCxfConsumerExample();
    example.run(args);
  }

  @Override
  public ODataConsumer create(String endpointUri, FormatType formatType, String methodToTunnel) {
    return ODataCxfConsumer.create(endpointUri);
  }
}
