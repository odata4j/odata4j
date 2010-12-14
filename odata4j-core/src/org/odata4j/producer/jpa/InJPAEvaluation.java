package org.odata4j.producer.jpa;

import org.odata4j.expression.AddExpression;
import org.odata4j.expression.AndExpression;
import org.odata4j.expression.BinaryCommonExpression;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.BoolParenExpression;
import org.odata4j.expression.BooleanLiteral;
import org.odata4j.expression.CommonExpression;
import org.odata4j.expression.DivExpression;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.expression.EqExpression;
import org.odata4j.expression.GeExpression;
import org.odata4j.expression.GtExpression;
import org.odata4j.expression.LeExpression;
import org.odata4j.expression.LiteralExpression;
import org.odata4j.expression.LtExpression;
import org.odata4j.expression.ModExpression;
import org.odata4j.expression.MulExpression;
import org.odata4j.expression.NeExpression;
import org.odata4j.expression.NotExpression;
import org.odata4j.expression.NullLiteral;
import org.odata4j.expression.OrExpression;
import org.odata4j.expression.ParenExpression;
import org.odata4j.expression.SubExpression;
import org.odata4j.expression.SubstringOfMethodCallExpression;
import org.odata4j.producer.resources.OptionsQueryParser;

public class InJPAEvaluation {

    public static String primaryKeyName;

    public static Object evaluate(CommonExpression expression) {

        if (expression instanceof BoolCommonExpression) {
            return evaluate((BoolCommonExpression) expression);
        }

        if (expression instanceof EntitySimpleProperty) {
            String field = ((EntitySimpleProperty) expression).getPropertyName();
            return "t." + (field.equals(OptionsQueryParser.PRIMARY_KEY_NAME)
                    ? primaryKeyName
                    : field.replace("/", "."));
        }

        if (expression instanceof NullLiteral) {
            return null;
        }

        if (expression instanceof LiteralExpression) {
            Object result = org.odata4j.expression.Expression.literalValue(
                    (LiteralExpression) expression);

            result = "'" + result + "'";
            return result;
        }

        if (expression instanceof AddExpression) {
            ObjectPair pair = createPair((BinaryCommonExpression) expression);
            return String.format("%1s + %2s", pair.lhs, pair.rhs);
        }

        if (expression instanceof SubExpression) {
            ObjectPair pair = createPair((BinaryCommonExpression) expression);
            return String.format("%1s - %2s", pair.lhs, pair.rhs);
        }

        if (expression instanceof MulExpression) {
            ObjectPair pair = createPair((BinaryCommonExpression) expression);
            return String.format("%1s * %2s", pair.lhs, pair.rhs);
        }

        if (expression instanceof DivExpression) {
            ObjectPair pair = createPair((BinaryCommonExpression) expression);
            return String.format("%1s / %2s", pair.lhs, pair.rhs);
        }

        if (expression instanceof ModExpression) {
            ObjectPair pair = createPair((BinaryCommonExpression) expression);
            return String.format("MOD(%1s, %2s)", pair.lhs, pair.rhs);
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
            return String.format("%1s = %2s", pair.lhs, pair.rhs);
        }

        if (expression instanceof NeExpression) {
            ObjectPair pair = createPair((NeExpression) expression);
            return String.format("%1s <> %2s", pair.lhs, pair.rhs);
        }
        if (expression instanceof AndExpression) {
            AndExpression e = (AndExpression) expression;
            return String.format(
                    "%1s AND %2s",
                    evaluate(e.getLHS()),
                    evaluate(e.getRHS()));
        }
        if (expression instanceof OrExpression) {
            OrExpression e = (OrExpression) expression;
            return String.format(
                    "%1s OR %2s",
                    evaluate(e.getLHS()),
                    evaluate(e.getRHS()));
        }
        if (expression instanceof BooleanLiteral) {
            return Boolean.toString(((BooleanLiteral) expression).getValue());
        }

        if (expression instanceof GtExpression) {
            ObjectPair pair = createPair((GtExpression) expression);
            return String.format("%1s > %2s", pair.lhs, pair.rhs);
        }
        if (expression instanceof LtExpression) {
            ObjectPair pair = createPair((LtExpression) expression);
            return String.format("%1s < %2s", pair.lhs, pair.rhs);
        }
        if (expression instanceof GeExpression) {
            ObjectPair pair = createPair((GeExpression) expression);
            return String.format("%1s >= %2s", pair.lhs, pair.rhs);
        }
        if (expression instanceof LeExpression) {
            ObjectPair pair = createPair((LeExpression) expression);
            return String.format("%1s <= %2s", pair.lhs, pair.rhs);
        }

        if (expression instanceof NotExpression) {
            NotExpression e = (NotExpression) expression;
            return String.format(
                    "NOT %1s",
                    evaluate(e.getExpression()));
        }

        if (expression instanceof SubstringOfMethodCallExpression) {
            // SubstringOfMethodCallExpression e
            // =(SubstringOfMethodCallExpression) expression;
            // String cbValue = (String) evaluate(e.getTarget());
            // String searchValue = (String) evaluate(e.getValue());
            // return cbValue != null && searchValue != null &&
            // cbValue.contains(searchValue);
            throw new UnsupportedOperationException(
                    "unsupported/tested expression " + expression);
        }

        if (expression instanceof ParenExpression) {
            ParenExpression e = (ParenExpression) expression;
            return "(" + evaluate((ParenExpression) e.getExpression()) + ")";
        }

        if (expression instanceof BoolParenExpression) {
            BoolParenExpression e = (BoolParenExpression) expression;
            return "(" + evaluate((BoolCommonExpression) e.getExpression()) + ")";
        }

        throw new UnsupportedOperationException("unsupported expression "
                + expression);
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
