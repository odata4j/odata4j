package org.odata4j.expression;

public interface ConcatMethodCallExpression extends MethodCallExpression {

    public abstract CommonExpression getLHS();

    public abstract CommonExpression getRHS();
}
