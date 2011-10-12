package org.odata4j.edm;

import org.odata4j.core.ImmutableList;

public class EdmAssociationSetEnd extends EdmItem {

  private final EdmAssociationEnd role;
  private final EdmEntitySet entitySet;

  private EdmAssociationSetEnd(EdmAssociationEnd role, EdmEntitySet entitySet,
      EdmDocumentation doc, ImmutableList<EdmAnnotation<?>> annots) {
    super(doc, annots);
    this.role = role;
    this.entitySet = entitySet;
  }

  public EdmAssociationEnd getRole() {
    return role;
  }

  public EdmEntitySet getEntitySet() {
    return entitySet;
  }

  public static Builder newBuilder() {
    return new Builder();
  }
  
  public static Builder newBuilder(EdmAssociationSetEnd associationSetEnd, BuilderContext context) {
    return context.newBuilder(associationSetEnd, new Builder());
  }

  public static class Builder extends EdmItem.Builder<EdmAssociationSetEnd, Builder> {

    private EdmAssociationEnd.Builder role;
    private String roleName;
    private EdmEntitySet.Builder entitySet;
    private String entitySetName;

    @Override
    public Builder newBuilder(EdmAssociationSetEnd associationSetEnd, BuilderContext context) {
      this.role = EdmAssociationEnd.newBuilder(associationSetEnd.role, context);
      this.entitySet = EdmEntitySet.newBuilder(associationSetEnd.entitySet, context);
      return this;
    }

    public Builder setRole(EdmAssociationEnd.Builder role) {
      this.role = role;
      return this;
    }

    public Builder setEntitySet(EdmEntitySet.Builder entitySet) {
      this.entitySet = entitySet;
      return this;
    }

    public EdmAssociationSetEnd build() {
      return new EdmAssociationSetEnd(role.build(), entitySet.build(), getDocumentation(), ImmutableList.copyOf(getAnnotations()));
    }

    public String getRoleName() {
      return roleName;
    }

    public Builder setRoleName(String roleName) {
      this.roleName = roleName;
      return this;
    }

    public Builder setEntitySetName(String entitySetName) {
      this.entitySetName = entitySetName;
      return this;
    }

    public String getEntitySetName() {
      return entitySetName;
    }

  }

}
