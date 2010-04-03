package org.odata4j.expression;

import java.util.UUID;

public interface GuidLiteral extends LiteralExpression {

    public abstract UUID getValue();
}
