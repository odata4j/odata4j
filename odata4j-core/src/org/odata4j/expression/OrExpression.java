package org.odata4j.expression;

public interface OrExpression extends BoolCommonExpression {

	public abstract BoolCommonExpression getLHS();
	public abstract BoolCommonExpression getRHS();
}
