package org.odata4j.producer.inmemory;

import java.math.BigDecimal;
import java.util.Set;

import org.odata4j.expression.*;

import core4j.Enumerable;

public class InMemoryEvaluation {

	public static Object evaluate(CommonExpression expression, Object target, PropertyModel properties){
		
		if (expression instanceof EntitySimpleProperty){
			return properties.getPropertyValue(target, ((EntitySimpleProperty)expression).getPropertyName());
		}
		if (expression instanceof StringLiteral){
			return ((StringLiteral)expression).getValue();
		}
		if (expression instanceof IntegralLiteral){
			return ((IntegralLiteral)expression).getValue();
		}
		if (expression instanceof AddExpression){
			return binaryFunction((BinaryCommonExpression)expression,target,properties,BinaryFunction.ADD);
		}
		if (expression instanceof SubExpression){
			return binaryFunction((BinaryCommonExpression)expression,target,properties,BinaryFunction.SUB);
		}
		if (expression instanceof MulExpression){
			return binaryFunction((BinaryCommonExpression)expression,target,properties,BinaryFunction.MUL);
		}
		if (expression instanceof DivExpression){
			return binaryFunction((BinaryCommonExpression)expression,target,properties,BinaryFunction.DIV);
		}
		if (expression instanceof ModExpression){
			return binaryFunction((BinaryCommonExpression)expression,target,properties,BinaryFunction.MOD);
		}
		if (expression instanceof ParenExpression){
			ParenExpression pe = (ParenExpression)expression;
			return evaluate(pe.getExpression(),target,properties);
		}
		throw new UnsupportedOperationException("unsupported expression " + expression);
	}
	
	@SuppressWarnings("unchecked")
	public static boolean evaluate(BoolCommonExpression expression, Object target, PropertyModel properties){
		if (expression instanceof EqExpression){
			return equals((EqExpression)expression,target,properties);
		}
		if (expression instanceof NeExpression){
			return !equals((NeExpression)expression,target,properties);
		}
		if (expression instanceof AndExpression){
			AndExpression e = (AndExpression)expression;
			return evaluate(e.getLHS(),target,properties) &&  evaluate(e.getRHS(),target,properties);
		}
		if (expression instanceof OrExpression){
			OrExpression e = (OrExpression)expression;
			return evaluate(e.getLHS(),target,properties) ||  evaluate(e.getRHS(),target,properties);
		}
		if (expression instanceof BooleanLiteral){
			return ((BooleanLiteral)expression).getValue();
		}
		
		if (expression instanceof GtExpression){
			return compareTo((GtExpression)expression,target,properties) > 0;
		}
		if (expression instanceof LtExpression){
			return compareTo((LtExpression)expression,target,properties) < 0;
		}
		if (expression instanceof GeExpression){
			return compareTo((GeExpression)expression,target,properties) >= 0;
		}
		if (expression instanceof LeExpression){
			return compareTo((LeExpression)expression,target,properties) <= 0;
		}
		throw new UnsupportedOperationException("unsupported expression " + expression);
	}	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private static interface BinaryFunction {
		public abstract BigDecimal apply(BigDecimal lhs, BigDecimal rhs);
		public abstract Double apply(Double lhs, Double rhs);
		public abstract Float apply(Float lhs, Float rhs);
		public abstract Integer apply(Integer lhs, Integer rhs);
		public abstract Long apply(Long lhs, Long rhs);
		
		public static final BinaryFunction ADD = new BinaryFunction(){
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
			}};
			
		public static final BinaryFunction SUB = new BinaryFunction(){
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
			}};
		
		public static final BinaryFunction MUL = new BinaryFunction(){
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
			}};
			
		public static final BinaryFunction DIV = new BinaryFunction(){
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
			}};
		public static final BinaryFunction MOD = new BinaryFunction(){
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
			}};
	}
	
	private static class ObjectPair {
		public Object lhs;
		public Object rhs;
		public ObjectPair(CommonExpression lhs, CommonExpression rhs,Object target, PropertyModel properties){
			this(evaluate(lhs,target,properties),evaluate(rhs,target,properties));
		}
		public ObjectPair(Object lhs, Object rhs){
			this.lhs = lhs;
			this.rhs = rhs;
		}
	}
	
	private static Object binaryFunction(BinaryCommonExpression be,Object target, PropertyModel properties, BinaryFunction function){
		ObjectPair pair = new ObjectPair(be.getLHS(),be.getRHS(),target,properties);
		binaryNumericPromotion(pair);

//		§ Edm.Decimal
//		§ Edm.Double
//		§ Edm.Single
//		§ Edm.Int32
//		§ Edm.Int64
		
		if (pair.lhs instanceof BigDecimal)
			return function.apply((BigDecimal)pair.lhs,(BigDecimal)pair.rhs);
		if (pair.lhs instanceof Double)
			return function.apply((Double)pair.lhs,(Double)pair.rhs);
		if (pair.lhs instanceof Float)
			return function.apply((Float)pair.lhs,(Float)pair.rhs);
		if (pair.lhs instanceof Integer)
			return function.apply((Integer)pair.lhs,(Integer)pair.rhs);
		if (pair.lhs instanceof Long)
			return function.apply((Long)pair.lhs,(Long)pair.rhs);
		
		throw new UnsupportedOperationException("unsupported add type " + pair.lhs);
	}
	

	
	
	private static boolean equals(BinaryCommonExpression be, Object target, PropertyModel properties){
		ObjectPair pair = new ObjectPair(be.getLHS(),be.getRHS(),target,properties);
		binaryNumericPromotion(pair);
		return (pair.lhs==null?pair.rhs==null:pair.lhs.equals(pair.rhs));
	}
	
	@SuppressWarnings("unchecked")
	private static int compareTo(BinaryCommonExpression be, Object target, PropertyModel properties){
		ObjectPair pair = new ObjectPair(be.getLHS(),be.getRHS(),target,properties);
		binaryNumericPromotion(pair);
		return ((Comparable)pair.lhs).compareTo(((Comparable)pair.rhs));
	}
	
	private static final Set<Class> SUPPORTED_CLASSES_FOR_BINARY_PROMOTION = Enumerable.create(
			BigDecimal.class,Double.class,Float.class,Byte.class,Integer.class,Short.class,Long.class
			).cast(Class.class).toSet();
	
	private static void binaryNumericPromotion(ObjectPair pair){
		
//		§ Edm.Decimal
//		§ Edm.Double
//		§ Edm.Single
//		§ Edm.Byte
//		§ Edm.Int16
//		§ Edm.Int32
//		§ Edm.Int64
		
		Class<?> lhsClass = pair.lhs.getClass();
		Class<?> rhsClass = pair.rhs.getClass();
		if (lhsClass.equals(rhsClass))
			return;
		if (!SUPPORTED_CLASSES_FOR_BINARY_PROMOTION.contains(lhsClass) || !SUPPORTED_CLASSES_FOR_BINARY_PROMOTION.contains(rhsClass))
			return;
		
		
//		If supported, binary numeric promotion SHOULD consist of the application of the following rules in the order specified:
		
//		§ If either operand is of type Edm.Decimal, the other operand is converted to Edm.Decimal unless it is of type Edm.Single or Edm.Double.
		if (lhsClass.equals(BigDecimal.class) && Enumerable.create(Byte.class,Short.class,Integer.class,Long.class).cast(Class.class).contains(rhsClass))
			 pair.rhs = BigDecimal.valueOf(((Number)pair.rhs).longValue());
		else if (rhsClass.equals(BigDecimal.class) && Enumerable.create(Byte.class,Short.class,Integer.class,Long.class).cast(Class.class).contains(lhsClass))
			 pair.lhs = BigDecimal.valueOf(((Number)pair.lhs).longValue());

//		§ Otherwise, if either operand is Edm.Double, the other operand is converted to type Edm.Double.
		else if (lhsClass.equals(Double.class))
			pair.rhs = ((Number)pair.rhs).doubleValue();
		else if (rhsClass.equals(Double.class))
			pair.lhs = ((Number)pair.lhs).doubleValue();
		
//		§ Otherwise, if either operand is Edm.Single, the other operand is converted to type Edm.Single.
		else if (lhsClass.equals(Float.class))
			pair.rhs = ((Number)pair.rhs).floatValue();
		else if (rhsClass.equals(Float.class))
			pair.lhs = ((Number)pair.lhs).floatValue();
		
//		§ Otherwise, if either operand is Edm.Int64, the other operand is converted to type Edm.Int64.
		else if (lhsClass.equals(Long.class))
			pair.rhs = ((Number)pair.rhs).longValue();
		else if (rhsClass.equals(Long.class))
			pair.lhs = ((Number)pair.lhs).longValue();
		
//		§ Otherwise, if either operand is Edm.Int32, the other operand is converted to type Edm.Int32
		else if (lhsClass.equals(Integer.class))
			pair.rhs = ((Number)pair.rhs).intValue();
		else if (rhsClass.equals(Integer.class))
			pair.lhs = ((Number)pair.lhs).intValue();
		
//		§ Otherwise, if either operand is Edm.Int16, the other operand is converted to type Edm.Int16.
		else if (lhsClass.equals(Short.class))
			pair.rhs = ((Number)pair.rhs).shortValue();
		else if (rhsClass.equals(Short.class))
			pair.lhs = ((Number)pair.lhs).shortValue();
		
//		§ If binary numeric promotion is supported, a data service SHOULD use a castExpression to promote an operand to the target type.
	
		
	}
	
	
	
	
}
