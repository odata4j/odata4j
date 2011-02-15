package org.odata4j.producer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.expression.OrderByExpression;

public class QueryInfo {

	public final InlineCount inlineCount;
	public final Integer top;
	public final Integer skip;
	public final BoolCommonExpression filter;
	public final List<OrderByExpression> orderBy;
	public final String skipToken;
	public final Map<String, String> customOptions;
	public final List<EntitySimpleProperty> expand;
	public final List<EntitySimpleProperty> select;

	public QueryInfo(
			InlineCount inlineCount,
			Integer top,
			Integer skip,
			BoolCommonExpression filter,
			List<OrderByExpression> orderBy,
			String skipToken,
			Map<String, String> customOptions,
			List<EntitySimpleProperty> expand,
			List<EntitySimpleProperty> select) {

		this.inlineCount = inlineCount;
		this.top = top;
		this.skip = skip;
		this.filter = filter;
		this.orderBy = orderBy;
		this.skipToken = skipToken;

		this.customOptions = Collections.unmodifiableMap(
				customOptions == null
						? new HashMap<String, String>() : customOptions);

		this.expand = Collections.unmodifiableList(
				expand == null
						? new ArrayList<EntitySimpleProperty>() : expand);

		this.select = Collections.unmodifiableList(
				select == null
						? new ArrayList<EntitySimpleProperty>() : select);
	}

}
