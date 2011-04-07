package org.odata4j.producer;

import java.util.Properties;

public interface ODataProducerFactory {

    public abstract ODataProducer create(Properties properties);
}
