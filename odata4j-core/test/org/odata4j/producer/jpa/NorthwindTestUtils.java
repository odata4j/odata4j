package org.odata4j.producer.jpa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.odata4j.edm.EdmType;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

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
    	return readFileToString(fileName, Charset.defaultCharset().name());
    }
    
    public static String readFileToString(String fileName, String charsetName) {
        StringBuilder strBuilder = new StringBuilder();
        try {
            InputStream buf =
                    NorthwindTestUtils.class.getResourceAsStream(fileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(buf, charsetName));
            String str;

            try {
                while ((str = in.readLine()) != null) {
                    strBuilder.append(str);
                }
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(JPAProducerQueryOptionTest.class.getName()).log(Level.SEVERE, null, ex);
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

        String result = webResource.accept("application/json").get(String.class);

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
        result = result.replace(
                "NorthwindModel.Categories",
                "NorthwindModel.Category");
        result = result.replace(
                "NorthwindModel.Products",
                "NorthwindModel.Product");
        result = result.replace(
                "NorthwindModel.Suppliers",
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
        expect = expect.replace(
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
    
    public static void TestAtomResult(String endpointUri, String uri, String inp) {
        System.out.println("Test: " + inp);

        String RESOURCES_TYPE = "xml";
        String RESOURCES_ROOT = "/META-INF/uri-conventions/";

        uri = uri.replace(" ", "%20");
        WebResource webResource = client.resource(endpointUri + uri);

        String result = webResource.accept("application/atom+xml").get(String.class);
        
        result = result.replace(
                "http://localhost:8810/northwind",
                "http://services.odata.org/northwind");
        
        String expect =
                NorthwindTestUtils.readFileToString(
                RESOURCES_ROOT + RESOURCES_TYPE + "/" + inp + "."
                + RESOURCES_TYPE, "utf-8");
        
        System.out.println("Expected:" + expect);
        System.out.println("Result:" + result);
        
        
        try {
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        	Document expectedDocument = factory.newDocumentBuilder().parse(new InputSource(new StringReader(expect)));
        	Document resultDocument = factory.newDocumentBuilder().parse(new InputSource(new StringReader(result)));
        	
        	assertEquals(expectedDocument.getDocumentElement(), resultDocument.getDocumentElement());
		} catch (Exception ex) {
			ex.printStackTrace();
			Assert.fail(ex.toString());
		}        
    }

    private static void assertEquals(Node expected, Node result) {
    	Assert.assertEquals(expected.getNodeType(), result.getNodeType());
    	Assert.assertEquals(expected.getLocalName(), result.getLocalName());
    	Assert.assertEquals(expected.getNamespaceURI(), result.getNamespaceURI());
    	
    	assertAttributesEquals(expected, result);
    	
    	List<Node> expectedLinks = new ArrayList<Node>();
    	List<Node> resultLinks = new ArrayList<Node>();
    	List<Node> expectedProperties = new ArrayList<Node>();
    	List<Node> resultProperties = new ArrayList<Node>();
    	
    	if (expected.getNodeType() == Node.TEXT_NODE) {
    		Assert.assertEquals(((Text)expected).getData(), ((Text)result).getData());
    	}
    	
    	int expectedIdx = 0;
    	int resultIdx = 0;
    	
    	while (expectedIdx < expected.getChildNodes().getLength()) {
    		//	skip the last newline inside the test data
    		if (expected.getChildNodes().item(expectedIdx).getNodeType() == Node.TEXT_NODE
    			&& resultIdx == result.getChildNodes().getLength()) {
    			expectedIdx++;
    			continue;
    		}
    		
    		// skip the newlines inside the test data
    		if (expected.getChildNodes().item(expectedIdx).getNodeType() == Node.TEXT_NODE
    			&& result.getChildNodes().item(resultIdx).getNodeType() != Node.TEXT_NODE) {
    			expectedIdx++;
    			Assert.assertTrue(expectedIdx < expected.getChildNodes().getLength());
    		}
    		Assert.assertTrue(resultIdx < result.getChildNodes().getLength());
    		
    		Node expectedChildNode = expected.getChildNodes().item(expectedIdx);
    		Node resultChildNode = result.getChildNodes().item(resultIdx);
    		
    		//	links and properties can be ordered differently in the expected and result data
    		if ("link".equals(expectedChildNode.getNodeName())) {
    			expectedLinks.add(expectedChildNode);
    			resultLinks.add(resultChildNode);
    		} else if ("m:properties".equals(expected.getNodeName())) {
    			expectedProperties.add(expectedChildNode);
    			resultProperties.add(resultChildNode);
    		} else if ("updated".equals(expected.getNodeName()))
    				; // ignore because time stamps differ always
    		else if (expected.getAttributes().getNamedItem("m:type") != null
    				&& EdmType.BINARY.toTypeString().equals(expected.getAttributes().getNamedItem("m:type").getNodeValue())
    				&& expectedChildNode.getNodeType() == Node.TEXT_NODE)
    		{
    			String s = ((Text)resultChildNode).getData();
    			s = s.replace("\r", "");
    			s = s.replace("\n", "");
	    		Assert.assertEquals(((Text)expectedChildNode).getData(), s);
    		//	Instead of normalizing the decimal's here (e.g "18.000" should be equal to "18") 
			//	we should probably add precision and scale to EdmType and regard these 
			//	when we write a decimal value
    		} else if (expected.getAttributes().getNamedItem("m:type") != null
    				&& EdmType.DECIMAL.toTypeString().equals(expected.getAttributes().getNamedItem("m:type").getNodeValue())
    				&& expectedChildNode.getNodeType() == Node.TEXT_NODE) {
    			Assert.assertEquals(Double.parseDouble(((Text)expectedChildNode).getData()), Double.parseDouble(((Text)resultChildNode).getData()), 0.00001);
    		} else
    			assertEquals(expectedChildNode, resultChildNode);
    		
    		expectedIdx++;
    		resultIdx++;
    	}
    	
    	Assert.assertEquals(expected.getChildNodes().getLength(), expectedIdx);
    	
    	if (result.getChildNodes().getLength() != resultIdx) {
    		System.out.println(result.getChildNodes().item(result.getChildNodes().getLength() - 1).getNodeType());
    	}
    	
    	Assert.assertEquals(result.getChildNodes().getLength(), resultIdx);
    	
    	//	compare the properties
    	Assert.assertEquals(expectedProperties.size(), resultProperties.size());
    	assertPropertyNodesEquals(expectedProperties, resultProperties);

    	//	compare the links
    	Assert.assertEquals(expectedLinks.size(), resultLinks.size());
    	assertLinkNodesEquals(expectedLinks, resultLinks);
    }
    
    private static void assertAttributesEquals(Node expected, Node result) {
    	NamedNodeMap expectedAttributes = expected.getAttributes();
    	NamedNodeMap resultAttributes = result.getAttributes();

    	// if both are null it's OK
    	if (expectedAttributes != null || resultAttributes != null) {
    		Assert.assertNotNull(expectedAttributes);
    		Assert.assertNotNull(resultAttributes);
	    	Assert.assertEquals(expectedAttributes.getLength(), resultAttributes.getLength());
	    	for (int i = 0; i < expectedAttributes.getLength(); i++) {
	    		Attr attr = (Attr)resultAttributes.getNamedItem(expectedAttributes.item(i).getNodeName());
	    		Assert.assertNotNull(attr);
	    		String expectedValue = ((Attr)expectedAttributes.item(i)).getValue();
	    		String resultValue = attr.getValue();
	    		
	    		//	different naming
	    		if ("category".equals(expected.getNodeName())) {
	    			resultValue = resultValue.replace("NorthwindModel.Categories", "NorthwindModel.Category");
	    			resultValue = resultValue.replace("NorthwindModel.Products", "NorthwindModel.Product");
	    			resultValue = resultValue.replace("NorthwindModel.Suppliers", "NorthwindModel.Supplier");
	    		}
	    		else if ("link".equals(expected.getNodeName()) && "title".equals(attr.getName()) 
	    				&& "edit".equals(((Attr)expected.getAttributes().getNamedItem("rel")).getNodeValue()) ) {
	    			resultValue = resultValue.replace("Categories", "Category");
	    			resultValue = resultValue.replace("Products", "Product");
	    		} else if ("link".equals(expected.getNodeName()) 
	    				&& "next".equals(((Attr)expected.getAttributes().getNamedItem("rel")).getNodeValue())
	    				&& "href".equals(attr.getName())) {
	    			resultValue = expectedValue; // we are using a different $skiptoken mechanism
	    		}

	    		
	    		Assert.assertEquals(expectedValue, resultValue);
	    	}
    	}    	
	}
    
    private static void assertPropertyNodesEquals(List<Node> expected, List<Node> result) {
    	Assert.assertEquals(expected.size(), result.size());
    	
    	//	sort the properties alphabetically
    	Comparator<Node> comparator = new Comparator<Node>() {

			@Override
			public int compare(Node n1, Node n2) {
				return n1.getNodeName().compareTo(n2.getNodeName());
			}
		};
    	
		Collections.sort(expected, comparator);
		Collections.sort(result, comparator);    	
    	
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), result.get(i));
		}
    }
    
    private static void assertLinkNodesEquals(List<Node> expected, List<Node> result) {
    	Assert.assertEquals(expected.size(), result.size());
    	
    	//	sort the links by the value of the title attributes
    	if (expected.size() > 1) {
	    	Comparator<Node> comparator = new Comparator<Node>() {
	
				@Override
				public int compare(Node n1, Node n2) {
					if (n1.getAttributes().getNamedItem("title") != null
						&& n2.getAttributes().getNamedItem("title") != null) {
						return n1.getAttributes().getNamedItem("title").getNodeValue()
							.compareTo(n2.getAttributes().getNamedItem("title").getNodeValue());
					} else if (n1.getAttributes().getNamedItem("title") != null) {
						return 1;
					} else {
						return -1;
					}
					
				}
			};
	    	
			Collections.sort(expected, comparator);
			Collections.sort(result, comparator);    	
    	}
    	
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), result.get(i));
		}
    }
}
