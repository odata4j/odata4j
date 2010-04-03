package org.odata4j.expression;

public interface BooleanLiteral extends LiteralExpression, BoolCommonExpression {

	public abstract boolean getValue();
}
