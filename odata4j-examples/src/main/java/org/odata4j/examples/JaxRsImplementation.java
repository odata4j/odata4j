package org.odata4j.examples;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.cxf.consumer.ODataCxfConsumer;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public enum JaxRsImplementation {
  JERSEY, CXF;

  public ODataConsumer newConsumer(String serviceRootUri) {
    return newConsumerBuilder(serviceRootUri).build();
  }

  public ODataConsumer.Builder newConsumerBuilder(String serviceRootUri) {
    if (this == JERSEY)
      return ODataJerseyConsumer.newBuilder(serviceRootUri);
    if (this == CXF)
      return ODataCxfConsumer.newBuilder(serviceRootUri);
    throw new UnsupportedOperationException("No consumer implementation for " + this);
  }
}
