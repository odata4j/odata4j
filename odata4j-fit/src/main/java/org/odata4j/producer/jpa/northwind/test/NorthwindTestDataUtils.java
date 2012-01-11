package org.odata4j.producer.jpa.northwind.test;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;

public class NorthwindTestDataUtils {

  public static final String RESOURCES_ROOT = "/META-INF/uri-conventions/";

  public static void fillDatabase(EntityManagerFactory emf) {
    try {
      Class.forName("org.hsqldb.jdbcDriver");
    } catch (Exception ex) {
      System.out.println("ERROR: failed to load HSQLDB JDBC driver.");
      Logger.getLogger(NorthwindTestDataUtils.class.getName()).log(
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

      InputStream xml = NorthwindTestDataUtils.class.getResourceAsStream(
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
      Logger.getLogger(NorthwindTestDataUtils.class.getName()).log(
          Level.SEVERE,
          null,
          ex);

    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException ex) {
          Logger.getLogger(NorthwindTestDataUtils.class.getName()).log(
              Level.SEVERE,
              null,
              ex);
        }
      }
    }
  }
  public static void writeStringToFile(String fileName, String contents) {
    Writer out = null;
    try {
      out = new OutputStreamWriter(new FileOutputStream(fileName), "utf-8");
      out.write(contents);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {}
      }
    }

  }

  public static String readFileToString(String fileName) {
    return readFileToString(fileName, Charset.defaultCharset().name());
  }

  public static String readFileToString(String fileName, String charsetName) {
    StringBuilder strBuilder = new StringBuilder();
    try {
      InputStream buf = NorthwindTestDataUtils.class.getResourceAsStream(
          fileName);

      BufferedReader in = new BufferedReader(
          new InputStreamReader(buf, charsetName));

      String str;

      try {
        while ((str = in.readLine()) != null) {
          strBuilder.append(str);
        }
        in.close();

      } catch (IOException ex) {
        Logger.getLogger(
            NorthwindTestDataUtils.class.getName()).log(
            Level.SEVERE,
            null,
            ex);
      }

    } catch (Exception ex) {
      Logger.getLogger(
          NorthwindTestDataUtils.class.getName()).log(
          Level.SEVERE,
          null,
          ex);
    }

    return strBuilder.toString();
  }


}
