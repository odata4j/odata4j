package org.odata4j.producer.jpa;

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
import org.odata4j.producer.resources.OptionsQueryParser;

public class InJPAEvaluation {

	public static String primaryKeyName;
	public static String tableAlias;

	public static Object evaluate(CommonExpression expression) {

		
		if (expression instanceof BoolCommonExpression) {
			return evaluate((BoolCommonExpression) expression);
		}

		if (expression instanceof EntitySimpleProperty) {
			String field = ((EntitySimpleProperty) expression)
					.getPropertyName();

			field = field.equals(OptionsQueryParser.PRIMARY_KEY_NAME)
					? primaryKeyName
					: field.replace("/", ".");

			return tableAlias + "." + field;
		}

		if (expression instanceof NullLiteral || expression == null) {
			return null;
		}

		if (expression instanceof LiteralExpression) {
			Object result = org.odata4j.expression.Expression.literalValue(
					(LiteralExpression) expression);

			if (result instanceof String) {
				result = "'" + result + "'";
			} else if (result instanceof Long) {
				result = result + "L";
			}

			return result;
		}

		if (expression instanceof AddExpression) {
			ObjectPair pair = createPair((BinaryCommonExpression) expression);
			return String.format("%s + %s", pair.lhs, pair.rhs);
		}

		if (expression instanceof SubExpression) {
			ObjectPair pair = createPair((BinaryCommonExpression) expression);
			return String.format("%s - %s", pair.lhs, pair.rhs);
		}

		if (expression instanceof MulExpression) {
			ObjectPair pair = createPair((BinaryCommonExpression) expression);
			return String.format("%s * %s", pair.lhs, pair.rhs);
		}

		if (expression instanceof DivExpression) {
			ObjectPair pair = createPair((BinaryCommonExpression) expression);
			return String.format("%s / %s", pair.lhs, pair.rhs);
		}

		if (expression instanceof ModExpression) {
			ObjectPair pair = createPair((BinaryCommonExpression) expression);
			return String.format("MOD(%s, %s)", pair.lhs, pair.rhs);
		}

		if (expression instanceof LengthMethodCallExpression) {
			LengthMethodCallExpression e = (LengthMethodCallExpression) expression;

			return String.format(
					"LENGTH(%s)",
					evaluate(e.getTarget()));
		}

		if (expression instanceof IndexOfMethodCallExpression) {
			IndexOfMethodCallExpression e = (IndexOfMethodCallExpression) expression;

			return String.format(
					"(LOCATE(%s, %s) - 1)",
					evaluate(e.getValue()),
					evaluate(e.getTarget()));
		}

		if (expression instanceof SubstringMethodCallExpression) {
			SubstringMethodCallExpression e = (SubstringMethodCallExpression) expression;

			Object length = evaluate(e.getLength());
			length = length != null ? ", " + length : "";

			return String.format(
					"SUBSTRING(%s, %s + 1 %s)",
					evaluate(e.getTarget()),
					evaluate(e.getStart()),
					length);
		}

		if (expression instanceof ToLowerMethodCallExpression) {
			ToLowerMethodCallExpression e = (ToLowerMethodCallExpression) expression;

			return String.format(
					"LOWER(%s)",
					evaluate(e.getTarget()));
		}

		if (expression instanceof ToUpperMethodCallExpression) {
			ToUpperMethodCallExpression e = (ToUpperMethodCallExpression) expression;

			return String.format(
					"UPPER(%s)",
					evaluate(e.getTarget()));
		}

		if (expression instanceof TrimMethodCallExpression) {
			TrimMethodCallExpression e = (TrimMethodCallExpression) expression;

			return String.format(
					"TRIM(BOTH FROM %s)",
					evaluate(e.getTarget()));
		}

		if (expression instanceof ConcatMethodCallExpression) {
			ConcatMethodCallExpression e = (ConcatMethodCallExpression) expression;

			return String.format(
					"CONCAT(%s, %s)",
					evaluate(e.getLHS()),
					evaluate(e.getRHS()));
		}

		if (expression instanceof ReplaceMethodCallExpression) {
			ReplaceMethodCallExpression e = (ReplaceMethodCallExpression) expression;

			return String.format(
					"FUNC('REPLACE', %s, %s, %s)",
					evaluate(e.getTarget()),
					evaluate(e.getFind()),
					evaluate(e.getReplace()));
		}

		if (expression instanceof RoundMethodCallExpression) {
			RoundMethodCallExpression e = (RoundMethodCallExpression) expression;

			// TODO: don't work while HSQL implementation expecting ROUND(a ,b)
			return String.format(
					"FUNC('ROUND', %s)",
					evaluate(e.getTarget()));
		}

		if (expression instanceof DayMethodCallExpression) {
			DayMethodCallExpression e = (DayMethodCallExpression) expression;

			// TODO: don't work could be trim bug in EclipseLink ... or wrong
			// syntax here
			return String.format(
					"TRIM(LEADING '0' FROM SUBSTRING(%s, 9, 2))",
					evaluate(e.getTarget()));
		}

		if (expression instanceof ParenExpression) {
			return evaluate(((ParenExpression) expression).getExpression());
		}

		if (expression instanceof BoolParenExpression) {
			return evaluate(((BoolParenExpression) expression).getExpression());
		}

		throw new UnsupportedOperationException(
				"unsupported expression " + expression);
	}

	public static String evaluate(BoolCommonExpression expression) {
		if (expression instanceof EqExpression) {
			ObjectPair pair = createPair((EqExpression) expression);
			return String.format("%s = %s", pair.lhs, pair.rhs);
		}

		if (expression instanceof NeExpression) {
			ObjectPair pair = createPair((NeExpression) expression);
			return String.format("%s <> %s", pair.lhs, pair.rhs);
		}

		if (expression instanceof AndExpression) {
			AndExpression e = (AndExpression) expression;
			return String.format(
					"%s AND %s",
					evaluate(e.getLHS()),
					evaluate(e.getRHS()));
		}

		if (expression instanceof OrExpression) {
			OrExpression e = (OrExpression) expression;
			return String.format(
					"%s OR %s",
					evaluate(e.getLHS()),
					evaluate(e.getRHS()));
		}

		if (expression instanceof BooleanLiteral) {
			return Boolean.toString(((BooleanLiteral) expression).getValue());
		}

		if (expression instanceof GtExpression) {
			ObjectPair pair = createPair((GtExpression) expression);
			return String.format("%s > %s", pair.lhs, pair.rhs);
		}

		if (expression instanceof LtExpression) {
			ObjectPair pair = createPair((LtExpression) expression);
			return String.format("%s < %s", pair.lhs, pair.rhs);
		}

		if (expression instanceof GeExpression) {
			ObjectPair pair = createPair((GeExpression) expression);
			return String.format("%s >= %s", pair.lhs, pair.rhs);
		}

		if (expression instanceof LeExpression) {
			ObjectPair pair = createPair((LeExpression) expression);
			return String.format("%s <= %s", pair.lhs, pair.rhs);
		}

		if (expression instanceof NotExpression) {
			NotExpression e = (NotExpression) expression;
			return String.format(
					"NOT (%s = TRUE)",
					evaluate(e.getExpression()));
		}

		if (expression instanceof SubstringOfMethodCallExpression) {
			SubstringOfMethodCallExpression e = (SubstringOfMethodCallExpression) expression;

			String value = (String) evaluate(e.getValue());
			value = value.replace("'", "");

			return String.format(
					"(CASE WHEN %s LIKE '%%%s%%' THEN TRUE ELSE FALSE END)",
					evaluate(e.getTarget()),
					value);
		}

		if (expression instanceof EndsWithMethodCallExpression) {
			EndsWithMethodCallExpression e = (EndsWithMethodCallExpression) expression;

			String value = (String) evaluate(e.getValue());
			value = value.replace("'", "");

			return String.format(
					"(CASE WHEN %s LIKE '%%%s' THEN TRUE ELSE FALSE END)",
					evaluate(e.getTarget()),
					value);
		}

		if (expression instanceof StartsWithMethodCallExpression) {
			StartsWithMethodCallExpression e = (StartsWithMethodCallExpression) expression;

			String value = (String) evaluate(e.getValue());
			value = value.replace("'", "");

			return String.format(
					"(CASE WHEN %s LIKE '%s%%' THEN TRUE ELSE FALSE END)",
					evaluate(e.getTarget()),
					value);
		}

		if (expression instanceof IsofExpression) {
			IsofExpression e = (IsofExpression) expression;

			Object clazz = evaluate(e.getExpression());
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
			return "(" + evaluate((ParenExpression) e.getExpression()) + ")";
		}

		if (expression instanceof BoolParenExpression) {
			BoolParenExpression e = (BoolParenExpression) expression;
			return "(" + evaluate((BoolCommonExpression) e.getExpression())
					+ ")";
		}

		throw new UnsupportedOperationException(
				"unsupported expression " + expression);
	}

	private static class ObjectPair {

		public Object lhs;
		public Object rhs;

		public ObjectPair(CommonExpression lhs, CommonExpression rhs) {
			this(evaluate(lhs), evaluate(rhs));
		}

		public ObjectPair(Object lhs, Object rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
	}

	private static ObjectPair createPair(BinaryCommonExpression be) {
		ObjectPair pair = new ObjectPair(be.getLHS(), be.getRHS());
		return pair;
	}
}
