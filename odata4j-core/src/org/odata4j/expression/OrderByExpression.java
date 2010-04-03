package org.odata4j.expression;

public interface OrderByExpression extends CommonExpression {

    public abstract CommonExpression getExpression();

    public abstract boolean isAscending();
}
