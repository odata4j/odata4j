
package org.odata4j.edm;

/**
 * CSDL AnnotationAttribute
 * Note how the value must be a String.
 * 
 * @author Tony Rozga
 */
public class EdmAnnotationAttribute extends EdmAnnotation {
  public EdmAnnotationAttribute(String namespaceURI, String namespacePrefix, String name, String value) {
    super(namespaceURI, namespacePrefix, name, value);
  }
  
  @Override
  public boolean isSimple() {
    return true;
  }
}
