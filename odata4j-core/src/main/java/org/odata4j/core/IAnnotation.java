
package org.odata4j.core;

/**
 * A generic annotation that  lives in a namespace
 * Simple annotations can be represented by a simple data type like string or numbe
 * 
 * @author rozan04
 */
public interface IAnnotation {
  
  String getNamespaceURI();
  String getNamespacePrefix();
  String getLocalName();
  
  /**
   * get the value of the annotation.  Implementors should honor isSimple by 
   * returning a "simple" object whose toString() returns a non-structured thing.
   * 
   * @return - annotation value; 
   */
  Object getValue();
  boolean isSimple();
}
