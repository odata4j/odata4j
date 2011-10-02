package org.odata4j.edm;

import java.util.List;

/**
 * Non-primitive type in the EDM type system.
 */
public class EdmNonSimpleType extends EdmType {

  public EdmNonSimpleType(String fullyQualifiedTypeName) {
    this(fullyQualifiedTypeName, null, null);
  }

  public EdmNonSimpleType(String fullyQualifiedTypeName, EdmDocumentation documentation,
      List<EdmAnnotation<?>> annotations) {
    super(fullyQualifiedTypeName, documentation, annotations);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

}
