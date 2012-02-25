package org.odata4j.producer;

import java.util.List;
import java.util.Map;

import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.EntitySimpleProperty;

/**
 * <code>QueryInfo</code> represents an OData single-entity query as a strongly-typed immutable data structure.
 */
public class EntityQueryInfo extends QueryInfo {

  /**
   * Creates a new <code>EntityQueryInfo</code> instance.
   */
  public EntityQueryInfo(
      BoolCommonExpression filter,
      Map<String, String> customOptions,
      List<EntitySimpleProperty> expand,
      List<EntitySimpleProperty> select) {
    super(null, null, null, filter, null, null, customOptions, expand, select);
  }

}
