package org.odata4j.expression;

public interface AddExpression extends BoolCommonExpression {

	public abstract CommonExpression getLHS();
	public abstract CommonExpression getRHS();
}
