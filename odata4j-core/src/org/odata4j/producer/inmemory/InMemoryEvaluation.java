package org.odata4j.producer.inmemory;

import org.odata4j.expression.*;

public class InMemoryEvaluation {

	public static Object evaluate(CommonExpression expression, Object target, PropertyModel properties){
		
		if (expression instanceof EntitySimpleProperty){
			return properties.getPropertyValue(target, ((EntitySimpleProperty)expression).getPropertyName());
		}
		if (expression instanceof StringLiteral){
			return ((StringLiteral)expression).getValue();
		}
		throw new UnsupportedOperationException("unsupported expression " + expression);
	}
	
	public static boolean evaluate(BoolCommonExpression expression, Object target, PropertyModel properties){
		if (expression instanceof EqExpression){
			EqExpression e = (EqExpression)expression;
			Object lhs = evaluate(e.getLHS(),target,properties);
			Object rhs = evaluate(e.getRHS(),target,properties);
			return lhs==null?rhs==null:lhs.equals(rhs);
		}
		if (expression instanceof NeExpression){
			NeExpression e = (NeExpression)expression;
			Object lhs = evaluate(e.getLHS(),target,properties);
			Object rhs = evaluate(e.getRHS(),target,properties);
			return !(lhs==null?rhs==null:lhs.equals(rhs));
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
		
		throw new UnsupportedOperationException("unsupported expression " + expression);
	}
	
}
