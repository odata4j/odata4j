package org.odata4j.producer.jpa.northwind.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;

public class NorthwindTestDataUtil {

  public static final String RESOURCES_ROOT = "/META-INF/uri-conventions/";

  public static void fillDatabase(EntityManagerFactory emf) {
    try {
      Class.forName("org.hsqldb.jdbcDriver");
    } catch (Exception ex) {
      System.out.println("ERROR: failed to load HSQLDB JDBC driver.");
      Logger.getLogger(NorthwindTestDataUtil.class.getName()).log(
          Level.SEVERE,
          null,
          ex);

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

      InputStream xml = NorthwindTestDataUtil.class.getResourceAsStream(
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
      Logger.getLogger(NorthwindTestDataUtil.class.getName()).log(
          Level.SEVERE,
          null,
          ex);

    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException ex) {
          Logger.getLogger(NorthwindTestDataUtil.class.getName()).log(
              Level.SEVERE,
              null,
              ex);
        }
      }
    }
  }
}
