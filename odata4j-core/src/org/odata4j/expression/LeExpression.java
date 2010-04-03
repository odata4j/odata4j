package org.odata4j.expression;

public interface LeExpression extends BoolCommonExpression {

	public abstract CommonExpression getLHS();
	public abstract CommonExpression getRHS();
}
