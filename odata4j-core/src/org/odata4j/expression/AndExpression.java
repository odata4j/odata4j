package org.odata4j.expression;

public interface AndExpression extends BoolCommonExpression {

	public abstract BoolCommonExpression getLHS();
	public abstract BoolCommonExpression getRHS();
}
