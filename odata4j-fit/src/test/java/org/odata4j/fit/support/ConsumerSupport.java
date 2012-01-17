package org.odata4j.fit.support;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.format.FormatType;

public interface ConsumerSupport {
  ODataConsumer create(String endpointUri, FormatType formatType, String methodToTunnel);
}
