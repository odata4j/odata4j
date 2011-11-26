package org.odata4j.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.odata4j.format.xml.EdmxFormatParserTest;
import org.odata4j.format.xml.EdmxFormatWriterTest;
import org.odata4j.format.xml.MultipleWorkspacesTest;
import org.odata4j.producer.inmemory.InMemoryProducerTest;
import org.odata4j.producer.jpa.airline.test.CreateWithLinkTest;
import org.odata4j.producer.jpa.airline.test.EdmDateTimeTemporalTest;
import org.odata4j.producer.jpa.airline.test.EdmTimeTemporalTest;
import org.odata4j.producer.jpa.northwind.test.CompositeKeyEntityTest;
import org.odata4j.producer.jpa.northwind.test.ConsumerLinksTest;
import org.odata4j.producer.jpa.northwind.test.CreateTest;
import org.odata4j.producer.jpa.northwind.test.CreateWithLink2Test;
import org.odata4j.producer.jpa.northwind.test.DeleteTest;
import org.odata4j.producer.jpa.northwind.test.FunctionTest;
import org.odata4j.producer.jpa.northwind.test.LinksTest;
import org.odata4j.producer.jpa.northwind.test.QueryOptionAtomTest;
import org.odata4j.producer.jpa.northwind.test.QueryOptionAtomTest50;
import org.odata4j.producer.jpa.northwind.test.QueryOptionTest;
import org.odata4j.producer.jpa.northwind.test.QueryOptionTest50;
import org.odata4j.producer.jpa.northwind.test.ResourcePathTest;
import org.odata4j.producer.jpa.northwind.test.UpdateTest;
import org.odata4j.producer.jpa.oneoff01.Oneoff01_Unidirectional;
import org.odata4j.producer.jpa.oneoff02.Oneoff02_ManyToManyWithoutMappedName;
import org.odata4j.producer.jpa.oneoff03.Oneoff03_ManyToMany;
import org.odata4j.test.consumer.ClientFactoryTest;
import org.odata4j.test.core.AtomFeedFormatParserTest;
import org.odata4j.test.core.EdmSimpleTypeTest;
import org.odata4j.test.core.EdmTypeTest;
import org.odata4j.test.core.OCollectionsTest;
import org.odata4j.test.core.OEntityKeyTest;
import org.odata4j.test.core.OFunctionParametersTest;
import org.odata4j.test.core.OPropertiesTest;
import org.odata4j.test.core.OSimpleObjectsTest;
import org.odata4j.test.expression.DateTimeFormatTest;
import org.odata4j.test.expression.ExpressionTest;
import org.odata4j.test.expression.JsonTest;
import org.odata4j.test.expression.PojoTest;
import org.odata4j.test.expression.ScenarioTest;
import org.odata4j.test.expression.TypeConverterTest;
import org.odata4j.test.issues.Issue10;
import org.odata4j.test.issues.Issue13;
import org.odata4j.test.issues.Issue15;
import org.odata4j.test.issues.Issue16;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ExpressionTest.class,
    JsonTest.class,
    PojoTest.class,
    ScenarioTest.class,
    TypeConverterTest.class,
    Issue10.class,
    Issue13.class,
    Issue15.class,
    Issue16.class,
    ResourcePathTest.class,
    QueryOptionTest.class,
    QueryOptionTest50.class,
    QueryOptionAtomTest.class,
    QueryOptionAtomTest50.class,
    DateTimeFormatTest.class,
    CreateTest.class,
    UpdateTest.class,
    DeleteTest.class,
    EdmTimeTemporalTest.class,
    EdmDateTimeTemporalTest.class,
    CreateWithLinkTest.class,
    CreateWithLink2Test.class,
    OEntityKeyTest.class,
    CompositeKeyEntityTest.class,
    Oneoff01_Unidirectional.class,
    Oneoff02_ManyToManyWithoutMappedName.class,
    Oneoff03_ManyToMany.class,
    AtomFeedFormatParserTest.class,
    LinksTest.class,
    EdmxFormatParserTest.class,
    EdmxFormatWriterTest.class,
    FunctionTest.class,
    EdmSimpleTypeTest.class,
    EdmTypeTest.class,
    MultipleWorkspacesTest.class,
    ClientFactoryTest.class,
    OFunctionParametersTest.class,
    OCollectionsTest.class,
    OPropertiesTest.class,
    OSimpleObjectsTest.class,
    InMemoryProducerTest.class,
    ConsumerLinksTest.class
    })
public class OData4jTestSuite {

  public static enum JPAProvider {
    ECLIPSELINK("EclipseLink"),
    HIBERNATE("Hibernate");

    public final String caption;

    JPAProvider(String caption) {
      this.caption = caption;
    }
  }

  public static final JPAProvider JPA_PROVIDER;

  static {
    String prop = System.getProperty("jpa");
    if (JPAProvider.ECLIPSELINK.caption.equalsIgnoreCase(prop))
      JPA_PROVIDER = JPAProvider.ECLIPSELINK;
    else if (JPAProvider.HIBERNATE.caption.equalsIgnoreCase(prop))
      JPA_PROVIDER = JPAProvider.HIBERNATE;
    else
      JPA_PROVIDER = JPAProvider.ECLIPSELINK;
  }
}
