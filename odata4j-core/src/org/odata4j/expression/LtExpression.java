package org.odata4j.expression;

public interface LtExpression extends BoolCommonExpression {

	public abstract CommonExpression getLHS();
	public abstract CommonExpression getRHS();
}
