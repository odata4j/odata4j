
package org.odata4j.edm;

/**
 * CSDL AnnotationElement.
 * Value can be any type.
 * 
 * @author Tony Rozga
 */
public class EdmAnnotationElement extends EdmAnnotation {
  public EdmAnnotationElement(String namespaceURI, String namespacePrefix, String name, Object value) {
    super(namespaceURI, namespacePrefix, name, value);
  }
  
  @Override
  public boolean isSimple() {
    return false;
  }
}
