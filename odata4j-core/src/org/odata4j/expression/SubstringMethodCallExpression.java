package org.odata4j.expression;

public interface SubstringMethodCallExpression extends MethodCallExpression {

    public abstract CommonExpression getTarget();

    public abstract CommonExpression getStart(); // optional

    public abstract CommonExpression getLength(); // optional
}
