package org.odata4j.expression;

public interface IndexOfMethodCallExpression extends MethodCallExpression {

    public abstract CommonExpression getTarget();

    public abstract CommonExpression getValue();
}
