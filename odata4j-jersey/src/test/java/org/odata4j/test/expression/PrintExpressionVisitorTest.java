package org.odata4j.test.expression;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.core4j.Enumerable;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.odata4j.core.Guid;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.expression.CommonExpression;
import org.odata4j.expression.EqExpression;
import org.odata4j.expression.Expression;
import org.odata4j.expression.OrderByExpression;
import org.odata4j.expression.PrintExpressionVisitor;

public class PrintExpressionVisitorTest {

  @Test
  public void testExpressionParsing() {
    t(Expression.null_(), "null");

    t(Expression.boolean_(true), "boolean(true)");
    t(Expression.boolean_(false), "boolean(false)");

    t(Expression.string(""), "string()");
    t(Expression.string("foo"), "string(foo)");

    t(Expression.integral(0), "integral(0)");
    t(Expression.integral(2), "integral(2)");
    t(Expression.integral(-2), "integral(-2)");
    t(Expression.integral(222222222), "integral(222222222)");
    t(Expression.integral(-222222222), "integral(-222222222)");
    t(Expression.int64(-2), "int64(-2)");
    t(Expression.single(-2f), "single(-2.0)");
    t(Expression.single(-2.34f), "single(-2.34)");
    t(Expression.double_(-2.34d), "double(-2.34)");
    t(Expression.double_(-2E+1), "double(-20.0)");
    t(Expression.double_(2E-1), "double(0.2)");
    t(Expression.double_(-2.1E+1), "double(-21.0)");
    t(Expression.double_(-2.1E-1), "double(-0.21)");
    t(Expression.decimal(new BigDecimal("2")), "decimal(2)");
    t(Expression.decimal(new BigDecimal("2.34")), "decimal(2.34)");
    t(Expression.decimal(new BigDecimal("-2")), "decimal(-2)");
    t(Expression.decimal(new BigDecimal("-2.34")), "decimal(-2.34)");
    t(Expression.dateTime(new LocalDateTime("2008-10-13")), "datetime(2008-10-13T00:00:00)");

    t(Expression.time(new LocalTime("13:20:00")), "time(13:20:00)");
    t(Expression.guid(Guid.fromString("12345678-aaaa-bbbb-ccccddddffff")), "guid(12345678-aaaa-bbbb-ccccddddffff)");
    t(Expression.guid(Guid.fromString("bf4eeb4d-2ded-4aa6-a167-0571e1057e3b")), "guid(bf4eeb4d-2ded-4aa6-a167-0571e1057e3b)");

    t(Expression.binary(new byte[]{(byte) 0xff}), "binary(ff)");
    t(Expression.binary(new byte[]{(byte) 0x00, (byte) 0xaa, (byte) 0xff}), "binary(00aaff)");

    t(Expression.simpleProperty("LastName"), "simpleProperty(LastName)");

    t(Expression.eq(Expression.simpleProperty("LastName"), Expression.string("foo")), "eq(simpleProperty(LastName),string(foo))");
    t(Expression.eq(Expression.string("foo"), Expression.simpleProperty("LastName")), "eq(string(foo),simpleProperty(LastName))");

    t(Expression.ne(Expression.simpleProperty("LastName"), Expression.string("foo")), "ne(simpleProperty(LastName),string(foo))");

    EqExpression exp = Expression.eq(Expression.simpleProperty("a"), Expression.integral(1));
    t(Expression.and(exp, exp), "and(eq(simpleProperty(a),integral(1)),eq(simpleProperty(a),integral(1)))");
    t(Expression.or(exp, exp), "or(eq(simpleProperty(a),integral(1)),eq(simpleProperty(a),integral(1)))");
    t(Expression.or(exp, Expression.and(exp, exp)), "or(eq(simpleProperty(a),integral(1)),and(eq(simpleProperty(a),integral(1)),eq(simpleProperty(a),integral(1))))");
    t(Expression.or(Expression.and(exp, exp), exp), "or(and(eq(simpleProperty(a),integral(1)),eq(simpleProperty(a),integral(1))),eq(simpleProperty(a),integral(1)))");
    t(Expression.and(Expression.boolean_(true), Expression.boolean_(false)), "and(boolean(true),boolean(false))");

    t(Expression.lt(Expression.simpleProperty("a"), Expression.integral(1)), "lt(simpleProperty(a),integral(1))");
    t(Expression.gt(Expression.simpleProperty("a"), Expression.integral(1)), "gt(simpleProperty(a),integral(1))");
    t(Expression.le(Expression.simpleProperty("a"), Expression.integral(1)), "le(simpleProperty(a),integral(1))");
    t(Expression.ge(Expression.simpleProperty("a"), Expression.integral(1)), "ge(simpleProperty(a),integral(1))");

    t(Expression.add(Expression.integral(1), Expression.integral(2)), "add(integral(1),integral(2))");
    t(Expression.sub(Expression.integral(1), Expression.integral(2)), "sub(integral(1),integral(2))");
    t(Expression.mul(Expression.integral(1), Expression.integral(2)), "mul(integral(1),integral(2))");
    t(Expression.div(Expression.integral(1), Expression.integral(2)), "div(integral(1),integral(2))");
    t(Expression.mod(Expression.integral(1), Expression.integral(2)), "mod(integral(1),integral(2))");

    t(Expression.paren(Expression.null_()), "paren(null)");
    t(Expression.paren(Expression.paren(Expression.null_())), "paren(paren(null))");
    t(Expression.add(Expression.paren(Expression.integral(1)), Expression.paren(Expression.integral(2))), "add(paren(integral(1)),paren(integral(2)))");

    t(Expression.not(Expression.null_()), "not(null)");
    t(Expression.negate(Expression.simpleProperty("a")), "negate(simpleProperty(a))");
    t(Expression.cast(EdmSimpleType.STRING.getFullyQualifiedTypeName()), "cast(Edm.String)");
    t(Expression.cast(Expression.null_(), EdmSimpleType.STRING.getFullyQualifiedTypeName()), "cast(null,Edm.String)");
    t(Expression.isof(EdmSimpleType.STRING.getFullyQualifiedTypeName()), "isof(Edm.String)");

    t(Expression.endsWith(Expression.string("aba"), Expression.string("a")), "endswith(string(aba),string(a))");
    t(Expression.eq(Expression.endsWith(Expression.string("aba"), Expression.string("a")), Expression.boolean_(true)), "eq(endswith(string(aba),string(a)),boolean(true))");
    t(Expression.startsWith(Expression.string("aba"), Expression.string("a")), "startswith(string(aba),string(a))");
    t(Expression.eq(Expression.startsWith(Expression.string("aba"), Expression.string("a")), Expression.boolean_(true)), "eq(startswith(string(aba),string(a)),boolean(true))");
    t(Expression.substringOf(Expression.string("aba"), Expression.string("a")), "substringof(string(aba),string(a))");
    t(Expression.eq(Expression.substringOf(Expression.string("aba"), Expression.string("a")), Expression.boolean_(true)), "eq(substringof(string(aba),string(a)),boolean(true))");
    t(Expression.indexOf(Expression.string("aba"), Expression.string("a")), "indexof(string(aba),string(a))");
    t(Expression.replace(Expression.string("aba"), Expression.string("a"), Expression.string("b")), "replace(string(aba),string(a),string(b))");
    t(Expression.toLower(Expression.string("aba")), "tolower(string(aba))");
    t(Expression.toUpper(Expression.string("aba")), "toupper(string(aba))");
    t(Expression.trim(Expression.string("aba")), "trim(string(aba))");
    t(Expression.substring(Expression.string("aba"), Expression.integral(1)), "substring(string(aba),integral(1))");
    t(Expression.substring(Expression.string("aba"), Expression.integral(1), Expression.integral(2)), "substring(string(aba),integral(1),integral(2))");
    t(Expression.concat(Expression.string("a"), Expression.string("b")), "concat(string(a),string(b))");
    t(Expression.length(Expression.string("aba")), "length(string(aba))");

    t(Expression.substringOf(Expression.simpleProperty("Name"), Expression.string("Boris")), "substringof(simpleProperty(Name),string(Boris))");

    t(Expression.year(Expression.string("aba")), "year(string(aba))");
    t(Expression.month(Expression.string("aba")), "month(string(aba))");
    t(Expression.day(Expression.string("aba")), "day(string(aba))");
    t(Expression.hour(Expression.string("aba")), "hour(string(aba))");
    t(Expression.minute(Expression.string("aba")), "minute(string(aba))");
    t(Expression.second(Expression.string("aba")), "second(string(aba))");
    t(Expression.round(Expression.string("aba")), "round(string(aba))");
    t(Expression.ceiling(Expression.string("aba")), "ceiling(string(aba))");
    t(Expression.floor(Expression.string("aba")), "floor(string(aba))");

    o("orderBy(simpleProperty(a),desc)", Expression.orderBy(Expression.simpleProperty("a"), OrderByExpression.Direction.DESCENDING));
    o("orderBy(simpleProperty(a),asc)", Expression.orderBy(Expression.simpleProperty("a"), OrderByExpression.Direction.ASCENDING));
    o("orderBy(simpleProperty(b),desc),orderBy(simpleProperty(a),asc)", Expression.orderBy(Expression.simpleProperty("b"), OrderByExpression.Direction.DESCENDING), Expression.orderBy(Expression.simpleProperty("a"), OrderByExpression.Direction.ASCENDING));
  }

  private void t(CommonExpression ce, String expected) {
    PrintExpressionVisitor visitor = new PrintExpressionVisitor();
    ce.visit(visitor);
    String actual = visitor.toString();
    Assert.assertEquals(expected, actual);
  }

  private void o(String expected, OrderByExpression... expressions) {
    List<String> values = new ArrayList<String>();

    for (OrderByExpression expr : expressions) {
      PrintExpressionVisitor visitor = new PrintExpressionVisitor();
      expr.visit(visitor);
      values.add(visitor.toString());
    }

    String actual = Enumerable.create(values).join(",");
    Assert.assertEquals(expected, actual);
  }

}
