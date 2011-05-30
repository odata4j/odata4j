package org.odata4j.producer.jpa;

import java.sql.Timestamp;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.expression.AddExpression;
import org.odata4j.expression.AndExpression;
import org.odata4j.expression.BinaryCommonExpression;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.BoolParenExpression;
import org.odata4j.expression.BooleanLiteral;
import org.odata4j.expression.CommonExpression;
import org.odata4j.expression.ConcatMethodCallExpression;
import org.odata4j.expression.DayMethodCallExpression;
import org.odata4j.expression.DivExpression;
import org.odata4j.expression.EndsWithMethodCallExpression;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.expression.EqExpression;
import org.odata4j.expression.GeExpression;
import org.odata4j.expression.GtExpression;
import org.odata4j.expression.IndexOfMethodCallExpression;
import org.odata4j.expression.IsofExpression;
import org.odata4j.expression.LeExpression;
import org.odata4j.expression.LengthMethodCallExpression;
import org.odata4j.expression.LiteralExpression;
import org.odata4j.expression.LtExpression;
import org.odata4j.expression.ModExpression;
import org.odata4j.expression.MulExpression;
import org.odata4j.expression.NeExpression;
import org.odata4j.expression.NotExpression;
import org.odata4j.expression.NullLiteral;
import org.odata4j.expression.OrExpression;
import org.odata4j.expression.ParenExpression;
import org.odata4j.expression.ReplaceMethodCallExpression;
import org.odata4j.expression.RoundMethodCallExpression;
import org.odata4j.expression.StartsWithMethodCallExpression;
import org.odata4j.expression.SubExpression;
import org.odata4j.expression.SubstringMethodCallExpression;
import org.odata4j.expression.SubstringOfMethodCallExpression;
import org.odata4j.expression.ToLowerMethodCallExpression;
import org.odata4j.expression.ToUpperMethodCallExpression;
import org.odata4j.expression.TrimMethodCallExpression;

public class JPQLGenerator {

  private final String primaryKeyName;
  private final String tableAlias;

  public JPQLGenerator(String primaryKeyName, String tableAlias) {
    this.primaryKeyName = primaryKeyName;
    this.tableAlias = tableAlias;
  }

  public String getPrimaryKeyName() {
    return primaryKeyName;
  }

  public String getTableAlias() {
    return tableAlias;
  }

  public static String toJpqlLiteral(Object value) {
    if (value instanceof String)
      return "'" + value + "'";
    if (value instanceof Long)
      return value + "L";
    if (value instanceof LocalTime)
      return "'" + new java.sql.Time(new LocalDateTime(
          ((LocalTime) value).getMillisOfDay(), DateTimeZone.UTC)
          .toDateTime().getMillis()).toString() + "'";
    if (value instanceof LocalDateTime) {
      java.sql.Timestamp d = new Timestamp(((LocalDateTime) value).toDateTime().getMillis());
      return "'" + d + "'";
    }
    return value.toString();
  }

  public String toJpql(CommonExpression expression) {

    if (expression instanceof BoolCommonExpression)
      return toJpql((BoolCommonExpression) expression);

    if (expression instanceof EntitySimpleProperty) {
      String field = ((EntitySimpleProperty) expression).getPropertyName();

      field = field.equals(primaryKeyName)
          ? primaryKeyName
          : field.replace("/", ".");

      return tableAlias + "." + field;
    }

    if (expression instanceof NullLiteral || expression == null)
      return null;

    if (expression instanceof LiteralExpression) {
      Object lValue = org.odata4j.expression.Expression.literalValue((LiteralExpression) expression);
      return toJpqlLiteral(lValue);
    }

    if (expression instanceof AddExpression)
      return bceToJpql("%s + %s", (AddExpression) expression);

    if (expression instanceof SubExpression)
      return bceToJpql("%s - %s", (SubExpression) expression);

    if (expression instanceof MulExpression)
      return bceToJpql("%s * %s", (MulExpression) expression);

    if (expression instanceof DivExpression)
      return bceToJpql("%s / %s", (DivExpression) expression);

    if (expression instanceof ModExpression)
      return bceToJpql("MOD(%s, %s)", (ModExpression) expression);

    if (expression instanceof LengthMethodCallExpression) {
      LengthMethodCallExpression e = (LengthMethodCallExpression) expression;

      return String.format(
          "LENGTH(%s)",
          toJpql(e.getTarget()));
    }

    if (expression instanceof IndexOfMethodCallExpression) {
      IndexOfMethodCallExpression e = (IndexOfMethodCallExpression) expression;

      return String.format(
          "(LOCATE(%s, %s) - 1)",
          toJpql(e.getValue()),
          toJpql(e.getTarget()));
    }

    if (expression instanceof SubstringMethodCallExpression) {
      SubstringMethodCallExpression e = (SubstringMethodCallExpression) expression;

      String length = toJpql(e.getLength());
      length = length != null ? ", " + length : "";

      return String.format(
          "SUBSTRING(%s, %s + 1 %s)",
          toJpql(e.getTarget()),
          toJpql(e.getStart()),
          length);
    }

    if (expression instanceof ToLowerMethodCallExpression) {
      ToLowerMethodCallExpression e = (ToLowerMethodCallExpression) expression;

      return String.format(
          "LOWER(%s)",
          toJpql(e.getTarget()));
    }

    if (expression instanceof ToUpperMethodCallExpression) {
      ToUpperMethodCallExpression e = (ToUpperMethodCallExpression) expression;

      return String.format(
          "UPPER(%s)",
          toJpql(e.getTarget()));
    }

    if (expression instanceof TrimMethodCallExpression) {
      TrimMethodCallExpression e = (TrimMethodCallExpression) expression;

      return String.format(
          "TRIM(BOTH FROM %s)",
          toJpql(e.getTarget()));
    }

    if (expression instanceof ConcatMethodCallExpression) {
      ConcatMethodCallExpression e = (ConcatMethodCallExpression) expression;

      return String.format(
          "CONCAT(%s, %s)",
          toJpql(e.getLHS()),
          toJpql(e.getRHS()));
    }

    if (expression instanceof ReplaceMethodCallExpression) {
      ReplaceMethodCallExpression e = (ReplaceMethodCallExpression) expression;

      return String.format(
          "FUNC('REPLACE', %s, %s, %s)",
          toJpql(e.getTarget()),
          toJpql(e.getFind()),
          toJpql(e.getReplace()));
    }

    if (expression instanceof RoundMethodCallExpression) {
      RoundMethodCallExpression e = (RoundMethodCallExpression) expression;

      // TODO: don't work while HSQL implementation expecting ROUND(a ,b)
      return String.format(
          "FUNC('ROUND', %s)",
          toJpql(e.getTarget()));
    }

    if (expression instanceof DayMethodCallExpression) {
      DayMethodCallExpression e = (DayMethodCallExpression) expression;

      // TODO: don't work could be trim bug in EclipseLink ... or wrong
      // syntax here
      return String.format(
          "TRIM(LEADING '0' FROM SUBSTRING(%s, 9, 2))",
          toJpql(e.getTarget()));
    }

    if (expression instanceof ParenExpression) {
      return toJpql(((ParenExpression) expression).getExpression());
    }

    if (expression instanceof BoolParenExpression) {
      return toJpql(((BoolParenExpression) expression).getExpression());
    }

    throw new UnsupportedOperationException("unsupported expression " + expression);
  }

  public String toJpql(BoolCommonExpression expression) {
    if (expression instanceof EqExpression)
      return bceToJpql("%s = %s", (EqExpression) expression);

    if (expression instanceof NeExpression)
      return bceToJpql("%s <> %s", (NeExpression) expression);

    if (expression instanceof AndExpression) {
      AndExpression e = (AndExpression) expression;
      return String.format(
          "%s AND %s",
          toJpql(e.getLHS()),
          toJpql(e.getRHS()));
    }

    if (expression instanceof OrExpression) {
      OrExpression e = (OrExpression) expression;
      return String.format(
          "%s OR %s",
          toJpql(e.getLHS()),
          toJpql(e.getRHS()));
    }

    if (expression instanceof BooleanLiteral)
      return Boolean.toString(((BooleanLiteral) expression).getValue());

    if (expression instanceof GtExpression)
      return bceToJpql("%s > %s", (GtExpression) expression);

    if (expression instanceof LtExpression)
      return bceToJpql("%s < %s", (LtExpression) expression);

    if (expression instanceof GeExpression)
      return bceToJpql("%s >= %s", (GeExpression) expression);

    if (expression instanceof LeExpression)
      return bceToJpql("%s <= %s", (LeExpression) expression);

    if (expression instanceof NotExpression) {
      NotExpression e = (NotExpression) expression;
      return String.format(
          "NOT (%s = TRUE)",
          toJpql(e.getExpression()));
    }

    if (expression instanceof SubstringOfMethodCallExpression) {
      SubstringOfMethodCallExpression e = (SubstringOfMethodCallExpression) expression;

      String value = (String) toJpql(e.getValue());
      value = value.replace("'", "");

      return String.format(
          "(CASE WHEN %s LIKE '%%%s%%' THEN TRUE ELSE FALSE END)",
          toJpql(e.getTarget()),
          value);
    }

    if (expression instanceof EndsWithMethodCallExpression) {
      EndsWithMethodCallExpression e = (EndsWithMethodCallExpression) expression;

      String value = (String) toJpql(e.getValue());
      value = value.replace("'", "");

      return String.format(
          "(CASE WHEN %s LIKE '%%%s' THEN TRUE ELSE FALSE END)",
          toJpql(e.getTarget()),
          value);
    }

    if (expression instanceof StartsWithMethodCallExpression) {
      StartsWithMethodCallExpression e = (StartsWithMethodCallExpression) expression;

      String value = (String) toJpql(e.getValue());
      value = value.replace("'", "");

      return String.format(
          "(CASE WHEN %s LIKE '%s%%' THEN TRUE ELSE FALSE END)",
          toJpql(e.getTarget()),
          value);
    }

    if (expression instanceof IsofExpression) {
      IsofExpression e = (IsofExpression) expression;

      String clazz = toJpql(e.getExpression());
      if (clazz == null) {
        clazz = tableAlias;
      }

      // TODO: don't work, for me its bug in EclipseLink
      return String.format(
          "TYPE(%s) = '%s'",
          clazz,
          e.getType());
    }

    if (expression instanceof ParenExpression) {
      ParenExpression e = (ParenExpression) expression;
      return "(" + toJpql((ParenExpression) e.getExpression()) + ")";
    }

    if (expression instanceof BoolParenExpression) {
      BoolParenExpression e = (BoolParenExpression) expression;
      return "(" + toJpql((BoolCommonExpression) e.getExpression()) + ")";
    }

    throw new UnsupportedOperationException("unsupported expression " + expression);
  }

  private String bceToJpql(String format, BinaryCommonExpression bce) {
    return String.format(format, toJpql(bce.getLHS()), toJpql(bce.getRHS()));
  }
}
