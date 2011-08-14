package org.odata4j.core;

import org.odata4j.edm.EdmType;

/**
 * A homogeneous collection of OData objects of a given {@link EdmType}.
 * T is the type of instances in the collection.
 */
public interface OCollection<T extends OObject> extends OObject, Iterable<T> {

  public interface Builder<T extends OObject> {
    Builder<T> add(T value);

    OCollection<T> build();
  }

  int size();

}
