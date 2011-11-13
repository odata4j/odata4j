package org.odata4j.producer.jpa.northwind.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.ExpressionParser;
import org.odata4j.expression.OrderByExpression;
import org.odata4j.producer.jpa.JPASkipToken;
import org.odata4j.producer.jpa.JPQLGenerator;

/**
 *
 * @author rozan04
 */
public class JPASkipTokenTest extends JPAProducerTestBase {

  private static OEntity product;
  private static OEntity order;

  public JPASkipTokenTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    setUpClass(3);
    ODataConsumer consumer = ODataConsumer.create(endpointUri);
    product = consumer.getEntity("Products", OEntityKey.create("ProductID", new Integer(1))).execute();
    order = consumer.getEntity("Orders", OEntityKey.create("OrderID", new Integer(10248))).execute();
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  private void test(String orderBy, String skipTokenExpected, String expectFilter, OEntity lastEntity, String key) {
    List<OrderByExpression> orderByList = ExpressionParser.parseOrderBy(orderBy);
    String skipToken = JPASkipToken.create(orderByList, lastEntity);
    System.out.println("skipexp: " + skipTokenExpected);
    System.out.println("skipgot: " + skipToken);
    assertEquals(skipTokenExpected, skipToken);

    BoolCommonExpression e = JPASkipToken.parse(key, orderByList, skipToken);
    String filter = new JPQLGenerator("", "t").toJpql(e);

    System.out.println("      filter: " + filter);
    System.out.println("expectFilter: " + expectFilter);

    assertEquals(expectFilter, filter);
  }

  @Test
  public void testCreateNoSort() {

    test("",
        "1",
        "(t.ProductID > 1)",
        product,
        "ProductID");
  }

  @Test
  public void testCreateOneSortString() {

    test("ProductName",
        "'Chai',1",
        "(t.ProductName > 'Chai' OR (t.ProductName = 'Chai' AND t.ProductID > 1))",
        product,
        "ProductID");
  }

  @Test
  public void testCreateTwoSort() {

    test("ProductName,QuantityPerUnit",
        "'Chai','10 boxes x 20 bags',1",
        "(t.ProductName > 'Chai' OR " +
        "(t.ProductName = 'Chai' AND t.QuantityPerUnit > '10 boxes x 20 bags') OR " +
        "(t.QuantityPerUnit = '10 boxes x 20 bags' AND t.ProductName = 'Chai' AND t.ProductID > 1))",
        product,
        "ProductID");
  }

  @Test
  public void testCreateOneSortDecimal() {

    test("UnitPrice",
        "18M,1",
        "(t.UnitPrice > 18 OR (t.UnitPrice = 18 AND t.ProductID > 1))",
        product,
        "ProductID");
  }

  @Test
  public void testCreateOneSortDate() {

    test("OrderDate",
        "datetime'1996-07-04T00:00:00',10248",
        "(t.OrderDate > '1996-07-04 00:00:00.0' OR (t.OrderDate = '1996-07-04 00:00:00.0' AND t.OrderID > 10248))",
        order,
        "OrderID");
  }
}
