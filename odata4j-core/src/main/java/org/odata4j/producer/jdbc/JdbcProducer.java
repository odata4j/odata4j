package org.odata4j.producer.jdbc;

import org.odata4j.command.CommandExecution;
import org.odata4j.producer.command.CommandProducer;

public class JdbcProducer extends CommandProducer {

  public static class Builder {

    private Jdbc jdbc;

    public Builder jdbc(Jdbc jdbc) {
      this.jdbc = jdbc;
      return this;
    }

    public JdbcProducer build() {
      if (jdbc == null)
        throw new IllegalArgumentException("Jdbc is mandatory");

      JdbcProducerBackend jdbcBackend = new JdbcProducerBackend() {

        @Override
        public CommandExecution getCommandExecution() {
          return CommandExecution.DEFAULT;
        }

        @Override
        public Jdbc getJdbc() {
          return jdbc;
        }

      };
      return new JdbcProducer(jdbcBackend);
    }
    
  }

  private final JdbcProducerBackend jdbcBackend;

  protected JdbcProducer(JdbcProducerBackend jdbcBackend) {
    super(jdbcBackend);
    this.jdbcBackend = jdbcBackend;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public Jdbc getJdbc() {
    return jdbcBackend.getJdbc();
  }

}
