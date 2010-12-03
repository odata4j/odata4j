package org.odata4j.producer.jpa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;

import org.junit.Assert;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class NorthwindTestUtils {

    private static Client client = Client.create();

    public static void fillDatabase(EntityManagerFactory emf) {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (Exception ex) {
            System.out.println("ERROR: failed to load HSQLDB JDBC driver.");
            Logger.getLogger(NorthwindTestUtils.class.getName()).log(
                    Level.SEVERE, null, ex);
            return;
        }

        Connection conn = null;
        String line = "";
        try {
            conn =
                    DriverManager.getConnection("jdbc:hsqldb:mem:northwind",
                            "sa", "");
            Statement statement = conn.createStatement();

            InputStream xml = NorthwindTestUtils.class.getResourceAsStream(
                    "/META-INF/northwind_insert.sql");

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(xml));

            while ((line = br.readLine()) != null) {
                line = line.replace("`", "");
                line = line.replace(");", ")");
                line = line.replace("'0x", "'");

                if (line.length() > 5) {
                    statement.executeUpdate(line);
                }
            }

            br.close();

        } catch (Exception ex) {
            System.out.println(line);
            Logger.getLogger(NorthwindTestUtils.class.getName()).log(
                    Level.SEVERE, null, ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(NorthwindTestUtils.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static String readFileToString(String fileName) {
        StringBuilder strBuilder = new StringBuilder();
        try {
            InputStream buf =
                    NorthwindTestUtils.class.getResourceAsStream(fileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(buf));
            String str;

            try {
                while ((str = in.readLine()) != null) {
                    strBuilder.append(str);
                }
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(JPAProducerQueryOptionTest.class.getName())
                        .log(Level.SEVERE, null, ex);
            }

        } catch (Exception ex) {
            Logger.getLogger(JPAProducerQueryOptionTest.class.getName()).log(
                    Level.SEVERE, null, ex);
        }

        return strBuilder.toString();
    }

    public static void TestJSONResult(String endpointUri, String uri, String inp) {
        System.out.println("Test: " + inp);

        String RESOURCES_TYPE = "json";
        String RESOURCES_ROOT = "/META-INF/uri-conventions/";

        uri = uri.replace(" ", "%20");
        WebResource webResource = client.resource(endpointUri + uri);

        String result =
                webResource.accept("application/json").get(String.class);

        // ignore format for human read
        result = result.replace(", ", ",");
        result = result.replace(" ,", ",");
        result = result.replace(": ", ":");
        result = result.replace(" :", ":");
        result = result.replace("\r", "");
        result = result.replace("\n", "");

        result = result.replace("\\r", "");
        result = result.replace("\\n", "");

        // different naming
        result =
                result.replace("NorthwindModel.Categories",
                        "NorthwindModel.Category");
        result =
                result.replace("NorthwindModel.Products",
                        "NorthwindModel.Product");
        result =
                result.replace("NorthwindModel.Suppliers",
                        "NorthwindModel.Supplier");

        result = result.replace(
                "http://localhost:8810/northwind",
                "http://services.odata.org/northwind");

        String expect =
                NorthwindTestUtils.readFileToString(
                        RESOURCES_ROOT + RESOURCES_TYPE + "/" + inp + "."
                                + RESOURCES_TYPE);

        // replace braces for ignore fields order in json
        expect = expect.replace("}}]", "");
        expect = expect.replace("}}", "}");
        expect = expect.replace("}}", "}");

        result = result.replace("}}]", "");
        result = result.replace("}}", "}");
        result = result.replace("}}", "}");

        // TODO: __next
        expect =
                expect.replace(
                        ",\"__next\":\"http://services.odata.org/Northwind/Northwind.svc/Products?$orderby=ProductID&$skiptoken=20,20\"}",
                        "");

        // no result tag by MS (?)
        expect = expect.replace("{\"results\":", "");
        result = result.replace("{\"results\":", "");

        String[] resultParts = result.split(",");
        Arrays.sort(resultParts);

        String[] expectParts = expect.split(",");
        Arrays.sort(expectParts);

        Assert.assertArrayEquals(resultParts, expectParts);
    }
}
