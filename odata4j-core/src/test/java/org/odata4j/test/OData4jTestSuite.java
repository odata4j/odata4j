package org.odata4j.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.odata4j.producer.jpa.airline.test.CreateWithLinkTest;
import org.odata4j.producer.jpa.airline.test.EdmDateTimeTemporalTest;
import org.odata4j.producer.jpa.airline.test.EdmTimeTemporalTest;
import org.odata4j.producer.jpa.northwind.test.CreateTest;
import org.odata4j.producer.jpa.northwind.test.DeleteTest;
import org.odata4j.producer.jpa.northwind.test.IssuesPassingTest;
import org.odata4j.producer.jpa.northwind.test.QueryOptionAtomTest;
import org.odata4j.producer.jpa.northwind.test.QueryOptionAtomTest50;
import org.odata4j.producer.jpa.northwind.test.QueryOptionTest;
import org.odata4j.producer.jpa.northwind.test.QueryOptionTest50;
import org.odata4j.producer.jpa.northwind.test.ResourcePathTest;
import org.odata4j.producer.jpa.northwind.test.UpdateTest;
import org.odata4j.test.core.OEntityKeyTest;
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
	OEntityKeyTest.class,
	IssuesPassingTest.class
})

public class OData4jTestSuite {

}
