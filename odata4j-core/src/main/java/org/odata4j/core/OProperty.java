package org.odata4j.core;

import org.odata4j.edm.EdmType;

/**
 * An immutable OData property instance, consisting of a name, a strongly-typed value, and an edm-type.
 *
 * @param <T>  the java-type of the property
 * <p>The {@link OProperties} static factory class can be used to create <code>OProperty</code> instances.</p>
 * @see OProperties
 */
public interface OProperty<T> extends NamedValue<T> {

  /**
   * Gets the edm-type for this property.
   * 
   * @return the edm-type
   */
  EdmType getType();

}
