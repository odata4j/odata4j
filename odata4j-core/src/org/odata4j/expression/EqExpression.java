package org.odata4j.expression;

public interface EqExpression extends BoolCommonExpression {

	public abstract CommonExpression getLHS();
	public abstract CommonExpression getRHS();
}
