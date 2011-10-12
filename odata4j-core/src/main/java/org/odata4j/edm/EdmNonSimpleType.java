package org.odata4j.edm;

import org.odata4j.core.ImmutableList;

/**
 * Non-primitive type in the EDM type system.
 */
public class EdmNonSimpleType extends EdmType {

  public EdmNonSimpleType(String fullyQualifiedTypeName) {
    this(fullyQualifiedTypeName, null, null);
  }

  public EdmNonSimpleType(String fullyQualifiedTypeName, EdmDocumentation documentation,
      ImmutableList<EdmAnnotation<?>> annotations) {
    super(fullyQualifiedTypeName, documentation, annotations);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

}
