package org.odata4j.producer.inmemory;

import org.core4j.Func;
import org.core4j.Func1;

public class InMemoryEntityInfo<TEntity, TKey> {
  String entitySetName;
  Class<TKey> keyClass;
  Class<TEntity> entityClass;
  Func<Iterable<TEntity>> get;
  Func1<Object, TKey> id;
  PropertyModel properties;
}
