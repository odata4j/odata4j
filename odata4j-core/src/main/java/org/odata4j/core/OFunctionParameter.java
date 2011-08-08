package org.odata4j.core;

import org.odata4j.edm.EdmBaseType;

/**
 * An immutable service operation parameter, consisting of a name, a strongly-typed value, and an edm-type.
 *
 * @param <T>  the java-type of the property
 * <p>The {@link OFunctionParameters} static factory class can be used to create <code>OFunctionParameter</code> instances.</p>
 * @see OFunctionParameters
 */
public interface OFunctionParameter extends NamedValue<OObject> {

  /**
   * Gets the edm-type for this property.
   *
   * @return the edm-type
   */
  EdmBaseType getType();

  /**
   * formats the function parameter for including in a URI
   * 
   * @return - value formatted for URI 
   */
  String toURIString();
}
