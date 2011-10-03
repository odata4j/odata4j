package org.odata4j.edm;

public class EdmNavigationProperty extends EdmPropertyBase {

  private final EdmAssociation relationship;
  private final EdmAssociationEnd fromRole;
  private final EdmAssociationEnd toRole;

  public EdmNavigationProperty(
      String name,
      EdmAssociation relationship,
      EdmAssociationEnd fromRole,
      EdmAssociationEnd toRole) {
    super(null, null, name);
    this.relationship = relationship;
    this.fromRole = fromRole;
    this.toRole = toRole;
  }

  public EdmAssociation getRelationship() {
    return relationship;
  }

  public EdmAssociationEnd getFromRole() {
    return fromRole;
  }

  public EdmAssociationEnd getToRole() {
    return toRole;
  }

  @Override
  public String toString() {
    return String.format("EdmNavigationProperty[%s,rel=%s,from=%s,to=%s]", getName(), relationship, fromRole, toRole);
  }

}
