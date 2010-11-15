package org.odata4j.producer.jpa;

import java.math.BigDecimal;
import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.core4j.Enumerable;
import org.odata4j.expression.AddExpression;
import org.odata4j.expression.AndExpression;
import org.odata4j.expression.BinaryCommonExpression;
import org.odata4j.expression.BoolCommonExpression;
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

public class InJPAEvaluation {

    @SuppressWarnings("unchecked")
    private static final Set<Class> SUPPORTED_CLASSES_FOR_BINARY_PROMOTION = Enumerable.create(BigDecimal.class, Double.class, Float.class, Byte.class, Integer.class, Short.class, Long.class).cast(Class.class).toSet();

    public static Object evaluate(CommonExpression expression, CriteriaBuilder cb, Root<?> root) {

        if (expression instanceof BoolCommonExpression) {
            return evaluate((BoolCommonExpression) expression, cb, root);
        }

        if (expression instanceof EntitySimpleProperty) {
            return ((EntitySimpleProperty) expression).getPropertyName();
        }

        if (expression instanceof NullLiteral) {
            return null;
        }

        if (expression instanceof LiteralExpression) {
            return org.odata4j.expression.Expression.literalValue(
                    (LiteralExpression) expression);
        }

        if (expression instanceof AddExpression) {
            return binaryFunction((BinaryCommonExpression) expression, cb, root, BinaryFunction.ADD);
        }

        if (expression instanceof SubExpression) {
            return binaryFunction((BinaryCommonExpression) expression, cb, root, BinaryFunction.SUB);
        }

        if (expression instanceof MulExpression) {
            return binaryFunction((BinaryCommonExpression) expression, cb, root, BinaryFunction.MUL);
        }

        if (expression instanceof DivExpression) {
            return binaryFunction((BinaryCommonExpression) expression, cb, root, BinaryFunction.DIV);
        }

        if (expression instanceof ModExpression) {
            return binaryFunction((BinaryCommonExpression) expression, cb, root, BinaryFunction.MOD);
        }

        if (expression instanceof ParenExpression) {
            return evaluate(((ParenExpression) expression).getExpression(), cb, root);
        }

        throw new UnsupportedOperationException("unsupported expression " + expression);
    }

    public static Expression<Boolean> evaluate(BoolCommonExpression expression, CriteriaBuilder cb, Root<?> root) {
        if (expression instanceof EqExpression) {
            ObjectPair pair = createPair((EqExpression) expression, cb, root);
            return cb.equal(buildPathExpression(root, (String) pair.lhs), pair.rhs);
        }
        if (expression instanceof NeExpression) {
            ObjectPair pair = createPair((EqExpression) expression, cb, root);
            return cb.notEqual(buildPathExpression(root, (String) pair.lhs), pair.rhs);
        }
        if (expression instanceof AndExpression) {
            AndExpression e = (AndExpression) expression;
            return cb.and(
                    evaluate(e.getLHS(), cb, root),
                    evaluate(e.getRHS(), cb, root));
        }
        if (expression instanceof OrExpression) {
            OrExpression e = (OrExpression) expression;
            return cb.or(
                    evaluate(e.getLHS(), cb, root),
                    evaluate(e.getRHS(), cb, root));
        }
        if (expression instanceof BooleanLiteral) {
//            return ((BooleanLiteral) expression).getValue();
            throw new UnsupportedOperationException("unsupported/tested expression " + expression);
        }

        if (expression instanceof GtExpression) {
            ObjectPair pair = createPair((GtExpression) expression, cb, root);
            return cb.greaterThan(
                    InJPAEvaluation.<Long>buildPathExpression(root, (String) pair.lhs),
                    (Long) pair.rhs);
        }
        if (expression instanceof LtExpression) {
            ObjectPair pair = createPair((LtExpression) expression, cb, root);
            return cb.lessThan(
                    InJPAEvaluation.<Long>buildPathExpression(root, (String) pair.lhs), (Long) pair.rhs);
        }
        if (expression instanceof GeExpression) {
            ObjectPair pair = createPair((GeExpression) expression, cb, root);
            return cb.ge(
                    InJPAEvaluation.<Number>buildPathExpression(root, (String) pair.lhs),
                    (Number) pair.rhs);
        }
        if (expression instanceof LeExpression) {
            ObjectPair pair = createPair((LeExpression) expression, cb, root);
            return cb.le(
                    InJPAEvaluation.<Number>buildPathExpression(root, (String) pair.lhs),
                    (Number) pair.rhs);
        }

        if (expression instanceof NotExpression) {
            NotExpression e = (NotExpression) expression;
            throw new UnsupportedOperationException("unsupported/tested expression " + expression);
//            return cb.not((Expression<Boolean>) evaluate(
//                    e.getExpression(),
//                    cb,
//                    root));
        }
        if (expression instanceof SubstringOfMethodCallExpression) {
            SubstringOfMethodCallExpression e = (SubstringOfMethodCallExpression) expression;
            String cbValue = (String) evaluate(e.getTarget(), cb, root);
            String searchValue = (String) evaluate(e.getValue(), cb, root);
//            return cbValue != null && searchValue != null && cbValue.contains(searchValue);
            throw new UnsupportedOperationException("unsupported/tested expression " + expression);
        }

        if (expression instanceof ParenExpression) {
            ParenExpression e = (ParenExpression) expression;
            return evaluate((BoolCommonExpression) e.getExpression(), cb, root);
        }

        throw new UnsupportedOperationException("unsupported expression " + expression);
    }

    private static <T> Path<T> buildPathExpression(Root<?> root, String path) {
        Path<T> expPath = null;
        for (String prop : path.split("/")) {
            if (expPath == null) {
                expPath = root.<T>get(prop);
            } else {
                expPath = expPath.<T>get(prop);
            }
        }

        return expPath;
    }

    private static interface BinaryFunction {

        public abstract BigDecimal apply(BigDecimal lhs, BigDecimal rhs);

        public abstract Double apply(Double lhs, Double rhs);

        public abstract Float apply(Float lhs, Float rhs);

        public abstract Integer apply(Integer lhs, Integer rhs);

        public abstract Long apply(Long lhs, Long rhs);
        public static final BinaryFunction ADD = new BinaryFunction() {

            public BigDecimal apply(BigDecimal lhs, BigDecimal rhs) {
                return lhs.add(rhs);
            }

            public Double apply(Double lhs, Double rhs) {
                return lhs + rhs;
            }

            public Float apply(Float lhs, Float rhs) {
                return lhs + rhs;
            }

            public Integer apply(Integer lhs, Integer rhs) {
                return lhs + rhs;
            }

            public Long apply(Long lhs, Long rhs) {
                return lhs + rhs;
            }
        };
        public static final BinaryFunction SUB = new BinaryFunction() {

            public BigDecimal apply(BigDecimal lhs, BigDecimal rhs) {
                return lhs.subtract(rhs);
            }

            public Double apply(Double lhs, Double rhs) {
                return lhs - rhs;
            }

            public Float apply(Float lhs, Float rhs) {
                return lhs - rhs;
            }

            public Integer apply(Integer lhs, Integer rhs) {
                return lhs - rhs;
            }

            public Long apply(Long lhs, Long rhs) {
                return lhs - rhs;
            }
        };
        public static final BinaryFunction MUL = new BinaryFunction() {

            public BigDecimal apply(BigDecimal lhs, BigDecimal rhs) {
                return lhs.multiply(rhs);
            }

            public Double apply(Double lhs, Double rhs) {
                return lhs * rhs;
            }

            public Float apply(Float lhs, Float rhs) {
                return lhs * rhs;
            }

            public Integer apply(Integer lhs, Integer rhs) {
                return lhs * rhs;
            }

            public Long apply(Long lhs, Long rhs) {
                return lhs * rhs;
            }
        };
        public static final BinaryFunction DIV = new BinaryFunction() {

            public BigDecimal apply(BigDecimal lhs, BigDecimal rhs) {
                return lhs.divide(rhs);
            }

            public Double apply(Double lhs, Double rhs) {
                return lhs / rhs;
            }

            public Float apply(Float lhs, Float rhs) {
                return lhs / rhs;
            }

            public Integer apply(Integer lhs, Integer rhs) {
                return lhs / rhs;
            }

            public Long apply(Long lhs, Long rhs) {
                return lhs / rhs;
            }
        };
        public static final BinaryFunction MOD = new BinaryFunction() {

            public BigDecimal apply(BigDecimal lhs, BigDecimal rhs) {
                return lhs.remainder(rhs);
            }

            public Double apply(Double lhs, Double rhs) {
                return lhs % rhs;
            }

            public Float apply(Float lhs, Float rhs) {
                return lhs % rhs;
            }

            public Integer apply(Integer lhs, Integer rhs) {
                return lhs % rhs;
            }

            public Long apply(Long lhs, Long rhs) {
                return lhs % rhs;
            }
        };
    }

    private static Object binaryFunction(BinaryCommonExpression be, CriteriaBuilder cb, Root<?> root, BinaryFunction function) {
        ObjectPair pair = new ObjectPair(be.getLHS(), be.getRHS(), cb, root);
        binaryNumericPromotion(pair);

        // § Edm.Decimal
        // § Edm.Double
        // § Edm.Single
        // § Edm.Int32
        // § Edm.Int64

        if (pair.lhs instanceof BigDecimal) {
            return function.apply((BigDecimal) pair.lhs, (BigDecimal) pair.rhs);
        }
        if (pair.lhs instanceof Double) {
            return function.apply((Double) pair.lhs, (Double) pair.rhs);
        }
        if (pair.lhs instanceof Float) {
            return function.apply((Float) pair.lhs, (Float) pair.rhs);
        }
        if (pair.lhs instanceof Integer) {
            return function.apply((Integer) pair.lhs, (Integer) pair.rhs);
        }
        if (pair.lhs instanceof Long) {
            return function.apply((Long) pair.lhs, (Long) pair.rhs);
        }

        throw new UnsupportedOperationException("unsupported add type " + pair.lhs);
    }

    @SuppressWarnings("unchecked")
    private static void binaryNumericPromotion(ObjectPair pair) {

        // § Edm.Decimal
        // § Edm.Double
        // § Edm.Single
        // § Edm.Byte
        // § Edm.Int16
        // § Edm.Int32
        // § Edm.Int64

        if (pair.lhs == null || pair.rhs == null) {
            return;
        }
        Class<?> lhsClass = pair.lhs.getClass();
        Class<?> rhsClass = pair.rhs.getClass();
        if (lhsClass.equals(rhsClass)) {
            return;
        }
        if (!SUPPORTED_CLASSES_FOR_BINARY_PROMOTION.contains(lhsClass) || !SUPPORTED_CLASSES_FOR_BINARY_PROMOTION.contains(rhsClass)) {
            return;
        }

        // If supported, binary numeric promotion SHOULD consist of the application of the following rules in the order specified:

        // § If either operand is of type Edm.Decimal, the other operand is converted to Edm.Decimal unless it is of type Edm.Single or Edm.Double.
        if (lhsClass.equals(BigDecimal.class) && Enumerable.create(Byte.class, Short.class, Integer.class, Long.class).cast(Class.class).contains(rhsClass)) {
            pair.rhs = BigDecimal.valueOf(((Number) pair.rhs).longValue());
        } else if (rhsClass.equals(BigDecimal.class) && Enumerable.create(Byte.class, Short.class, Integer.class, Long.class).cast(Class.class).contains(lhsClass)) {
            pair.lhs = BigDecimal.valueOf(((Number) pair.lhs).longValue());
        } // § Otherwise, if either operand is Edm.Double, the other operand is converted to type Edm.Double.
        else if (lhsClass.equals(Double.class)) {
            pair.rhs = ((Number) pair.rhs).doubleValue();
        } else if (rhsClass.equals(Double.class)) {
            pair.lhs = ((Number) pair.lhs).doubleValue();
        } // § Otherwise, if either operand is Edm.Single, the other operand is converted to type Edm.Single.
        else if (lhsClass.equals(Float.class)) {
            pair.rhs = ((Number) pair.rhs).floatValue();
        } else if (rhsClass.equals(Float.class)) {
            pair.lhs = ((Number) pair.lhs).floatValue();
        } // § Otherwise, if either operand is Edm.Int64, the other operand is converted to type Edm.Int64.
        else if (lhsClass.equals(Long.class)) {
            pair.rhs = ((Number) pair.rhs).longValue();
        } else if (rhsClass.equals(Long.class)) {
            pair.lhs = ((Number) pair.lhs).longValue();
        } // § Otherwise, if either operand is Edm.Int32, the other operand is converted to type Edm.Int32
        else if (lhsClass.equals(Integer.class)) {
            pair.rhs = ((Number) pair.rhs).intValue();
        } else if (rhsClass.equals(Integer.class)) {
            pair.lhs = ((Number) pair.lhs).intValue();
        } // § Otherwise, if either operand is Edm.Int16, the other operand is converted to type Edm.Int16.
        else if (lhsClass.equals(Short.class)) {
            pair.rhs = ((Number) pair.rhs).shortValue();
        } else if (rhsClass.equals(Short.class)) {
            pair.lhs = ((Number) pair.lhs).shortValue();
        }

        // § If binary numeric promotion is supported, a data service SHOULD use a castExpression to promote an operand to the cb type.

    }

    private static class ObjectPair {

        public Object lhs;
        public Object rhs;

        public ObjectPair(CommonExpression lhs, CommonExpression rhs, CriteriaBuilder cb, Root<?> root) {
            this(evaluate(lhs, cb, root), evaluate(rhs, cb, root));
        }

        public ObjectPair(Object lhs, Object rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }
    }

    private static ObjectPair createPair(BinaryCommonExpression be, CriteriaBuilder cb, Root<?> root) {
        ObjectPair pair = new ObjectPair(be.getLHS(), be.getRHS(), cb, root);
        binaryNumericPromotion(pair);

        return pair;
    }
}
