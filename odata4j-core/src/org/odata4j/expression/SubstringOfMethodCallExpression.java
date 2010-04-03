package org.odata4j.expression;

public interface SubstringOfMethodCallExpression extends BoolMethodExpression {

    public abstract CommonExpression getValue();

    public abstract CommonExpression getTarget(); // optional
}
