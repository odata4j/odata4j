package org.odata4j.edm;

import java.util.List;

public class EdmAssociationSetEnd extends EdmItem {

  private final EdmAssociationEnd role;
  private final EdmEntitySet entitySet;

  public EdmAssociationSetEnd(EdmAssociationEnd role, EdmEntitySet entitySet) {
    this(role, entitySet, null, null);
  }

  public EdmAssociationSetEnd(EdmAssociationEnd role, EdmEntitySet entitySet,
      EdmDocumentation doc, List<EdmAnnotation<?>> annots) {
    super(null, null);
    this.role = role;
    this.entitySet = entitySet;
  }

  public EdmAssociationEnd getRole() {
    return role;
  }

  public EdmEntitySet getEntitySet() {
    return entitySet;
  }

}
