package org.odata4j.edm;

import java.util.List;

public class EdmComplexType extends EdmStructuralType {

  public EdmComplexType(String namespace, String name, List<EdmProperty> properties) {
    this(namespace, name, properties, null, null);
  }

  public EdmComplexType(String namespace, String name, List<EdmProperty> properties, 
          EdmDocumentation documentation, List<EdmAnnotation> annots) {
    this(namespace, name, properties, documentation, annots, null);
  }
  
  public EdmComplexType(String namespace, String name, List<EdmProperty> properties, 
          EdmDocumentation documentation, List<EdmAnnotation> annots,
          Boolean isAbstract) {
    super(null, namespace, name, properties, documentation, annots, isAbstract);
  }
  
}
