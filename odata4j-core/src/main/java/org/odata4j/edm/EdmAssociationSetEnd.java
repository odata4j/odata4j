package org.odata4j.edm;

import java.util.List;

public class EdmAssociationSetEnd extends EdmItem {

  public final EdmAssociationEnd role;
  public final EdmEntitySet entitySet;

  public EdmAssociationSetEnd(EdmAssociationEnd role, EdmEntitySet entitySet) {
    this(role, entitySet, null, null);
  }
    
  public EdmAssociationSetEnd(EdmAssociationEnd role, EdmEntitySet entitySet,
          EdmDocumentation doc, List<EdmAnnotation> annots) {
    super(null, null); // TODO
    this.role = role;
    this.entitySet = entitySet;
  }
}
