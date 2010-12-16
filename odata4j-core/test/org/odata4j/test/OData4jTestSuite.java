package org.odata4j.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.odata4j.producer.jpa.JPAProducerQueryOptionAtomTest;
import org.odata4j.producer.jpa.JPAProducerQueryOptionAtomTest50;
import org.odata4j.producer.jpa.JPAProducerQueryOptionTest;
import org.odata4j.producer.jpa.JPAProducerQueryOptionTest50;
import org.odata4j.producer.jpa.JPAProducerResourcePathTest;
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
})

public class OData4jTestSuite {

}
