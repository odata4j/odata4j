package org.odata4j.expression;

public interface StartsWithMethodCallExpression extends BoolMethodExpression {

    public abstract CommonExpression getTarget();

    public abstract CommonExpression getValue();
}
