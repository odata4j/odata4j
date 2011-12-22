package org.odata4j.examples;

import org.odata4j.consumer.ODataConsumer;

public interface ConsumerExample {

    ODataConsumer create(String endpointUri);
    void run(String... args);
}
