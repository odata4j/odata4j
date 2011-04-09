package org.odata4j.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.odata4j.producer.jpa.airline.test.CreateWithLinkTest;
import org.odata4j.producer.jpa.airline.test.EdmDateTimeTemporalTest;
import org.odata4j.producer.jpa.airline.test.EdmTimeTemporalTest;
import org.odata4j.producer.jpa.northwind.JPAProducerCreateTest;
import org.odata4j.producer.jpa.northwind.JPAProducerQueryOptionAtomTest;
import org.odata4j.producer.jpa.northwind.JPAProducerQueryOptionAtomTest50;
import org.odata4j.producer.jpa.northwind.JPAProducerQueryOptionTest;
import org.odata4j.producer.jpa.northwind.JPAProducerQueryOptionTest50;
import org.odata4j.producer.jpa.northwind.JPAProducerResourcePathTest;
import org.odata4j.producer.jpa.northwind.JPAProducerUpdateTest;
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
	JPAProducerResourcePathTest.class,
	JPAProducerQueryOptionTest.class,
	JPAProducerQueryOptionTest50.class,
	JPAProducerQueryOptionAtomTest.class,
	JPAProducerQueryOptionAtomTest50.class,
	DateTimeFormatTest.class,
	JPAProducerCreateTest.class,
	JPAProducerUpdateTest.class,
	EdmTimeTemporalTest.class,
	EdmDateTimeTemporalTest.class,
	CreateWithLinkTest.class
})

public class OData4jTestSuite {

}
