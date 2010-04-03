package org.odata4j.producer;

import java.util.List;

import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.OrderByExpression;

public class QueryInfo {

    public final InlineCount inlineCount;
    public final Integer top;
    public final Integer skip;
    public final BoolCommonExpression filter;
    public final List<OrderByExpression> orderBy;

    public QueryInfo(InlineCount inlineCount, Integer top, Integer skip, BoolCommonExpression filter, List<OrderByExpression> orderBy) {
        this.inlineCount = inlineCount;
        this.top = top;
        this.skip = skip;
        this.filter = filter;
        this.orderBy = orderBy;
    }

}
