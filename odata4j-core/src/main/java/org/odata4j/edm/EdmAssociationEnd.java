package org.odata4j.edm;

import java.util.List;

public class EdmAssociationEnd extends EdmItem {

  private final String role;
  private final EdmEntityType type;
  private final EdmMultiplicity multiplicity;

  public EdmAssociationEnd(String role, EdmEntityType type, EdmMultiplicity multiplicity) {
    this(role, type, multiplicity, null, null);
  }

  public EdmAssociationEnd(String role, EdmEntityType type, EdmMultiplicity multiplicity,
      EdmDocumentation doc, List<EdmAnnotation<?>> annots) {
    super(doc, annots);
    this.role = role;
    this.type = type;
    this.multiplicity = multiplicity;
  }

  public String getRole() {
    return role;
  }

  public EdmEntityType getType() {
    return type;
  }

  public EdmMultiplicity getMultiplicity() {
    return multiplicity;
  }

  @Override
  public String toString() {
    return String.format("EdmAssociationEnd[%s,%s,%s]", role, type, multiplicity);
  }

}
