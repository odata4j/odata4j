package org.odata4j.expression;

public interface DivExpression extends BoolCommonExpression {

	public abstract CommonExpression getLHS();
	public abstract CommonExpression getRHS();
}
