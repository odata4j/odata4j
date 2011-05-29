package org.odata4j.core;

// TODO(0.5) javadoc
public interface OEntityGetRequest<T> extends OEntityRequest<T> {

  OEntityRequest<T> select(String select);

  OEntityRequest<T> expand(String expand);
}
