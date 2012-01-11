package org.odata4j.examples;

import org.odata4j.consumer.ODataConsumer;

public interface ConsumerSupport {
    ODataConsumer create(String endpointUri);
}
