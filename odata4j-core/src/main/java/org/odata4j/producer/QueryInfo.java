package org.odata4j.producer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.expression.OrderByExpression;

/**
 * <code>QueryInfo</code> represents an OData query as a strongly-typed immutable data structure.
 */
public class QueryInfo {

	/**
	 * The $inlinecount value, if present.
	 * 
	 * @see <a href="http://www.odata.org/developers/protocols/uri-conventions#InlinecountSystemQueryOption">[odata.org] Inlinecount System Query Option ($inlinecount)</a>
	 */
	public final InlineCount inlineCount;
	
	/**
	 * The number of items to return, if present.
	 * 
	 * @see <a href="http://www.odata.org/developers/protocols/uri-conventions#TopSystemQueryOption">[odata.org] Top System Query Option ($top)</a>
	 */
	public final Integer top;
	
	/**
	 * The number of items to skip, if present.
	 * 
	 * @see <a href="http://www.odata.org/developers/protocols/uri-conventions#SkipSystemQueryOption">[odata.org] Skip System Query Option ($skip)</a>
	 */
	public final Integer skip;
	
	/**
	 * The filter expression to apply, if present.
	 * 
	 * @see <a href="http://www.odata.org/developers/protocols/uri-conventions#FilterSystemQueryOption">[odata.org] Filter System Query Option ($filter)</a>
	 */
	public final BoolCommonExpression filter;
	
	/**
	 * The ordering expressions to apply, if present.
	 * 
	 * @see <a href="http://www.odata.org/developers/protocols/uri-conventions#OrderBySystemQueryOption">[odata.org] Orderby System Query Option ($orderby)</a>
	 */
	public final List<OrderByExpression> orderBy;
	
	/**
	 * The continuation token to use as a starting point, if present.
	 */
	public final String skipToken;
	
	/**
	 * Custom name-value pairs, if present.
	 * 
	 * @see <a href="http://www.odata.org/developers/protocols/uri-conventions#CustomQueryOptions">[odata.org] Custom Query Options</a>
	 */
	public final Map<String, String> customOptions;
	
	/**
	 * Expand expressions, if present.
	 * 
	 * @see <a href="http://www.odata.org/developers/protocols/uri-conventions#ExpandSystemQueryOption">[odata.org] Expand System Query Option ($expand)</a>
	 */
	public final List<EntitySimpleProperty> expand;
	
	/**
	 * Selection clauses, if present.
	 * 
	 * @see <a href="http://www.odata.org/developers/protocols/uri-conventions#SelectSystemQueryOption">[odata.org] Select System Query Option ($select)</a>
	 */
	public final List<EntitySimpleProperty> select;

	/**
	 * Creates a new <code>QueryInfo</code> instance.
	 */
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
