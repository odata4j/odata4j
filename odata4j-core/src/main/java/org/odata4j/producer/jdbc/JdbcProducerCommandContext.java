package org.odata4j.producer.jdbc;

public interface JdbcProducerCommandContext {

  Jdbc getJdbc();

  JdbcProducerBackend getBackend();

}
