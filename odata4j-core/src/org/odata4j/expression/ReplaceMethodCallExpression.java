package org.odata4j.expression;

public interface ReplaceMethodCallExpression extends MethodCallExpression {

	public abstract CommonExpression getTarget();
	public abstract CommonExpression getFind();
	public abstract CommonExpression getReplace();
}
