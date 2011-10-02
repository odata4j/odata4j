
package org.odata4j.core;

/**
 * Implementation of {@link Annotation}
 */
public class GenericAnnotation<T> implements Annotation<T> {

  private String namespaceUri;
  private String namespacePrefix;
  private String localName;
  private Class<T> valueType;
  private T value;

  public GenericAnnotation(String namespaceUri, String namespacePrefix, String localName, Class<T> valueType, T value) {
    this.namespaceUri = namespaceUri;
    this.localName = localName;
    this.namespacePrefix = namespacePrefix;
    this.valueType = valueType;
    this.value = value;
  }

  public String getNamespaceUri() {
    return namespaceUri;
  }

  public String getNamespacePrefix() {
    return namespacePrefix;
  }

  public String getLocalName() {
    return localName;
  }

  public String getFullyQualifiedName() {
    return namespaceUri + ":" + localName;
  }

  public Class<T> getValueType() {
    return valueType;
  }

  public T getValue() {
    return value;
  }

}
