package org.odata4j.expression;

public interface EndsWithMethodCallExpression extends BoolMethodExpression {

	public abstract CommonExpression getTarget();
	public abstract CommonExpression getValue();
}
