package org.odata4j.producer.inmemory;

import java.util.HashMap;

import org.core4j.Func;
import org.core4j.Func1;

public class InMemoryEntityInfo<TEntity> {

  String entitySetName;
  String entityTypeName;
  String[] keys;
  Class<TEntity> entityClass;
  Func<Iterable<TEntity>> get;
  Func1<Object, HashMap<String, Object>> id;
  PropertyModel properties;
  boolean hasStream;
}
