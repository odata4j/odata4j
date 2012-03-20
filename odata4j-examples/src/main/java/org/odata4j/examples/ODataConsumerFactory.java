package org.odata4j.examples;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.behaviors.MethodTunnelingBehavior;
import org.odata4j.cxf.consumer.ODataCxfConsumer;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class ODataConsumerFactory {

  final JaxRsImplementation impl;

  public ODataConsumerFactory(JaxRsImplementation impl) {
    this.impl = impl;
  }

  public ODataConsumer createODataConsumer(String endpointUri, FormatType format, String methodToTunnel) {
    switch (impl) {
    case JERSEY:
      return createJerseyConsumer(endpointUri, format, methodToTunnel);
    case CXF:
      return createCxfConsumer(endpointUri, format, methodToTunnel);
    }
    return null;
  }

  private ODataJerseyConsumer createJerseyConsumer(String endpointUri, FormatType format, String methodToTunnel) {
    org.odata4j.jersey.consumer.ODataJerseyConsumer.Builder builder = ODataJerseyConsumer.newBuilder(endpointUri);

    if (format != null)
      builder = builder.setFormatType(format);

    if (methodToTunnel != null)
      builder = builder.setClientBehaviors(new MethodTunnelingBehavior(methodToTunnel));

    return builder.build();
  }

  private ODataCxfConsumer createCxfConsumer(String endpointUri, FormatType format, String methodToTunnel) {
    org.odata4j.cxf.consumer.ODataCxfConsumer.Builder builder = ODataCxfConsumer.newBuilder(endpointUri);

    if (format != null)
      builder = builder.setFormatType(format);

    if (methodToTunnel != null)
      builder = builder.setClientBehaviors(new MethodTunnelingBehavior(methodToTunnel));

    return builder.build();
  }
}
