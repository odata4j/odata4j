package org.odata4j.examples.producer.jpa.northwind;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NorthwindUtils {

  protected static final Logger LOGGER = LoggerFactory.getLogger(NorthwindUtils.class);

  public static void fillDatabase(EntityManagerFactory emf) {
    try {
      Class.forName("org.hsqldb.jdbcDriver");
    } catch (Exception ex) {
      NorthwindUtils.LOGGER.error("ERROR: failed to load HSQLDB JDBC driver.", ex);
      return;
    }

    Connection conn = null;
    String line = "";
    try {
      conn = DriverManager.getConnection(
          "jdbc:hsqldb:mem:northwind",
          "sa",
          "");

      Statement statement = conn.createStatement();

      InputStream xml = NorthwindUtils.class.getResourceAsStream(
          "/META-INF/northwind_insert.sql");

      BufferedReader br = new BufferedReader(
          new InputStreamReader(xml, "UTF-8"));

      while ((line = br.readLine()) != null) {
        line = line.replace("`", "");
        line = line.replace(");", ")");
        line = line.replace("'0x", "'");

        if (line.length() > 5) {
          statement.executeUpdate(line);
        }
      }

      br.close();
      statement.close();

    } catch (Exception ex) {
      NorthwindUtils.LOGGER.error(ex.getMessage(), ex);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException ex) {
          NorthwindUtils.LOGGER.error(ex.getMessage(), ex);
        }
      }
    }
  }
}
