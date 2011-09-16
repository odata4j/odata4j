package org.odata4j.edm;

import org.odata4j.core.Annotation;

/**
 * An Annotation for use in annotating Edm constructs
 * 
 * @author Tony Rozga
 */
public class EdmAnnotation extends Annotation {
  public EdmAnnotation(String namespaceURI, String namespacePrefix, String name, Object value) {
    super(namespaceURI, namespacePrefix, name, value);
  }
}
