package org.odata4j.expression;

import org.joda.time.DateTime;

public interface DateTimeOffsetLiteral extends LiteralExpression {

    public abstract DateTime getValue();
}
