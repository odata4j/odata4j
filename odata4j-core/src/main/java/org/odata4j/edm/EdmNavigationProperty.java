package org.odata4j.edm;


public class EdmNavigationProperty extends EdmPropertyBase {

  private final EdmAssociation relationship;
  private final EdmAssociationEnd fromRole;
  private final EdmAssociationEnd toRole;

  private EdmNavigationProperty(
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

  public static Builder newBuilder(String name) {
    return new Builder(name);
  }
  
  public static Builder newBuilder(EdmNavigationProperty navigationProperty, BuilderContext context) {
    return context.newBuilder(navigationProperty, new Builder(navigationProperty.getName()));
  }
  
  public static class Builder extends EdmPropertyBase.Builder<EdmNavigationProperty, Builder> {

    private EdmAssociation.Builder relationship;
    private String relationshipName;
    private EdmAssociationEnd.Builder fromRole;
    private String fromRoleName;
    private EdmAssociationEnd.Builder toRole;
    private String toRoleName;

    private Builder(String name) {
      super(name);
    }

    @Override
    Builder newBuilder(EdmNavigationProperty navigationProperty, BuilderContext context) {
      this.relationship = EdmAssociation.newBuilder(navigationProperty.relationship, context);
      this.fromRole = EdmAssociationEnd.newBuilder(navigationProperty.fromRole, context);
      this.toRole = EdmAssociationEnd.newBuilder(navigationProperty.toRole, context);
      return this;
    }

    public Builder setRelationship(EdmAssociation.Builder relationship) {
      this.relationship = relationship;
      return this;
    }

    public Builder setFromTo(EdmAssociationEnd.Builder fromRole, EdmAssociationEnd.Builder toRole) {
      this.fromRole = fromRole;
      this.toRole = toRole;
      return this;
    }

    public EdmNavigationProperty build() {
      return new EdmNavigationProperty(getName(), relationship.build(), fromRole.build(), toRole.build());
    }
    
    public String getRelationshipName() {
      return relationshipName;
    }
    
    public String getFromRoleName() {
      return fromRoleName;
    }
    
    public String getToRoleName() {
      return toRoleName;
    }
    
    public Builder setRelationshipName(String relationshipName) {
      this.relationshipName = relationshipName;
      return this;
    }
    
    public Builder setFromToName(String fromRoleName, String toRoleName) {
      this.fromRoleName = fromRoleName;
      this.toRoleName = toRoleName;
      return this;
    }

  }

}
