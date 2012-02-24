package org.odata4j.producer.jpa.airline.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.persistence.EntityManagerFactory;

import org.core4j.ThrowingFunc1;
import org.junit.After;
import org.junit.Ignore;
import org.odata4j.producer.server.ODataServer;
import org.odata4j.test.AbstractRuntimeTest;

@Ignore
public class AirlineJPAProducerTestBase extends AbstractRuntimeTest {

  public AirlineJPAProducerTestBase(RuntimeFacadeType type) {
    super(type);
  }

  protected static final String endpointUri =
      "http://localhost:8810/airline/Airline.svc/";

  protected static EntityManagerFactory emf;
  protected static ODataServer server;

  protected void execute(ThrowingFunc1<Connection, Void> function) {
    try {
      Class.forName("org.hsqldb.jdbcDriver");
    } catch (Exception ex) {
      this.logger.error("ERROR: failed to load HSQLDB JDBC driver.", ex);
      return;
    }

    Connection conn = null;
    try {
      conn = DriverManager.getConnection(
          "jdbc:hsqldb:mem:airline",
          "sa",
          "");

      function.apply(conn);

    } catch (Exception ex) {
      this.logger.error(ex.getMessage(), ex);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException ex) {
          this.logger.error(ex.getMessage(), ex);
        }
      }
    }
  }

  //  @Before
  public void fillDatabase() {

    execute(new ThrowingFunc1<Connection, Void>() {

      @Override
      public Void apply(Connection conn) throws Exception {

        String line;
        Statement statement = conn.createStatement();

        BufferedReader br = null;
        try {
          InputStream xml = AirlineJPAProducerTestBase.class
              .getResourceAsStream("/META-INF/airline_insert.sql");

          br = new BufferedReader(new InputStreamReader(xml, "UTF-8"));

          while ((line = br.readLine()) != null) {
            if (line.length() > 5) {
              statement.executeUpdate(line);
            }
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        } finally {
          try {
            statement.close();
          } catch (Exception ignore) {}
          if (br != null)
            br.close();
        }

        return null;
      }
    });
  }

  @After
  public void clearDatabase() throws SQLException {

    execute(new ThrowingFunc1<Connection, Void>() {

      @Override
      public Void apply(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        try {
          statement.execute("DELETE FROM FLIGHT;");
          statement.execute("DELETE FROM FLIGHTSCHEDULE;");
          statement.execute("DELETE FROM AIRPORT;");
        } finally {
          statement.close();
        }
        return null;
      }
    });

    if (server != null) {
      server.stop();
    }

    if (emf != null) {
      emf.close();
    }

  }

}
