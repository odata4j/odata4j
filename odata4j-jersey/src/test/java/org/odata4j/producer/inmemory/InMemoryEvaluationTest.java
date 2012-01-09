package org.odata4j.producer.inmemory;

import org.junit.Assert;
import org.junit.Test;
import org.odata4j.expression.CommonExpression;
import org.odata4j.expression.EqExpression;
import org.odata4j.expression.Expression;

public class InMemoryEvaluationTest {

  @Test
  public void testLengthExpression() {
    EqExpression ex = Expression.eq(Expression.length(Expression.string("aaa")), Expression.int64(3));

    boolean evaluate = InMemoryEvaluation.evaluate(ex, this, new BeanBasedPropertyModel(getClass()));
    Assert.assertTrue(evaluate);
  }

  @Test
  public void testLengthExpression2() {
    CommonExpression ex = Expression.length(Expression.string("aaa"));

    Object evaluate = InMemoryEvaluation.evaluate(ex, this, new BeanBasedPropertyModel(getClass()));
    Assert.assertEquals(evaluate, (Integer)3);
  }

  @Test
  public void testToLowerExpression() {
    CommonExpression ex = Expression.toLower(Expression.string("QaQaQa"));

    Object evaluate = InMemoryEvaluation.evaluate(ex, this, new BeanBasedPropertyModel(getClass()));
    Assert.assertEquals(evaluate, "qaqaqa");
  }

  @Test
  public void testToUpperExpression() {
    CommonExpression ex = Expression.toUpper(Expression.string("QaQaQa"));

    Object evaluate = InMemoryEvaluation.evaluate(ex, this, new BeanBasedPropertyModel(getClass()));
    Assert.assertEquals(evaluate, "QAQAQA");
  }

  @Test
  public void testTrimExpression() {
    CommonExpression ex = Expression.trim(Expression.string(" QaQ aQa "));

    Object evaluate = InMemoryEvaluation.evaluate(ex, this, new BeanBasedPropertyModel(getClass()));
    Assert.assertEquals(evaluate, "QaQ aQa");
  }

  @Test
  public void testConcatExpression() {
    CommonExpression ex = Expression.concat(Expression.string("A"), Expression.string("B"));

    Object evaluate = InMemoryEvaluation.evaluate(ex, this, new BeanBasedPropertyModel(getClass()));
    Assert.assertEquals(evaluate, "AB");
  }

  @Test
  public void testReplaceExpression() {
    CommonExpression ex = Expression.replace(Expression.string("ABCDB"), Expression.string("B"), Expression.string("Q"));

    Object evaluate = InMemoryEvaluation.evaluate(ex, this, new BeanBasedPropertyModel(getClass()));
    Assert.assertEquals(evaluate, "AQCDQ");
  }

  @Test
  public void testIndexOfExpression() {
    CommonExpression ex = Expression.indexOf(Expression.string("ABCDB"), Expression.string("B"));

    Object evaluate = InMemoryEvaluation.evaluate(ex, this, new BeanBasedPropertyModel(getClass()));
    Assert.assertEquals(evaluate, Integer.valueOf(1));
  }

  @Test
  public void testSubstringExpression2() {
    CommonExpression ex = Expression.substring(Expression.string("ABCDB"), Expression.integral(2));

    Object evaluate = InMemoryEvaluation.evaluate(ex, this, new BeanBasedPropertyModel(getClass()));
    Assert.assertEquals(evaluate, "CDB");
  }

  @Test
  public void testSubstringExpression3() {
    CommonExpression ex = Expression.substring(Expression.string("ABCDB"), Expression.integral(2), Expression.integral(1));

    Object evaluate = InMemoryEvaluation.evaluate(ex, this, new BeanBasedPropertyModel(getClass()));
    Assert.assertEquals(evaluate, "C");
  }

}