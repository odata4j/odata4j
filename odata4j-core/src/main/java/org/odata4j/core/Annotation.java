
package org.odata4j.core;

/**
 * A generic annotation that lives in a namespace.
 */
public interface Annotation<T> {

  String getNamespaceUri();
  String getNamespacePrefix();
  String getLocalName();

  Class<T> getValueType();
  T getValue();
}
