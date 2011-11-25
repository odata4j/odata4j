package org.odata4j.edm;

import org.core4j.Func;
import org.odata4j.core.ImmutableList;

/**
 * A CSDL End element (as a child of the Association element)
 *
 * <p>An End element (as a child of the Association element) identifies the entity type on one end of
 * an association and the number of entity type instances that can exist at that end of an association.
 * Association ends are defined as part of an association; an association must have exactly two association
 * ends. Entity type instances at one end of an association can be accessed through navigation properties
 * or foreign keys if they are exposed on an entity type.
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb896235.aspx">[msdn] End Element (CSDL)</a>
 */
public class EdmAssociationEnd extends EdmItem {

  private final String role;
  private final Func<EdmEntityType> type;
  private final EdmMultiplicity multiplicity;

  private EdmAssociationEnd(String role, Func<EdmEntityType> type, EdmMultiplicity multiplicity,
      EdmDocumentation doc, ImmutableList<EdmAnnotation<?>> annots) {
    super(doc, annots);
    this.role = role;
    this.type = type;
    this.multiplicity = multiplicity;
  }

  public String getRole() {
    return role;
  }

  public EdmEntityType getType() {
    return type.apply();
  }

  public EdmMultiplicity getMultiplicity() {
    return multiplicity;
  }

  @Override
  public String toString() {
    return String.format("EdmAssociationEnd[%s,%s,%s]", role, type, multiplicity);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(EdmAssociationEnd associationEnd, BuilderContext context) {
    return context.newBuilder(associationEnd, new Builder());
  }

  /** Mutable builder for {@link EdmAssociationEnd} objects. */
  public static class Builder extends EdmItem.Builder<EdmAssociationEnd, Builder> {

    private String role;
    private EdmEntityType.Builder type;
    private String typeName;
    private EdmMultiplicity multiplicity;

    @Override
    Builder newBuilder(EdmAssociationEnd associationEnd, BuilderContext context) {
      this.role = associationEnd.role;
      this.type = EdmEntityType.newBuilder(associationEnd.getType(), context);
      this.multiplicity = associationEnd.multiplicity;
      return this;
    }

    public EdmAssociationEnd build() {
      return new EdmAssociationEnd(role, type == null ? null : type.builtFunc(), multiplicity, getDocumentation(), ImmutableList.copyOf(getAnnotations()));
    }

    public Builder setType(EdmEntityType.Builder type) {
      this.type = type;
      return this;
    }

    public Builder setTypeName(String typeName) {
      this.typeName = typeName;
      return this;
    }

    public Builder setMultiplicity(EdmMultiplicity multiplicity) {
      this.multiplicity = multiplicity;
      return this;
    }

    public EdmEntityType.Builder getType() {
      return type;
    }

    public String getTypeName() {
      return typeName;
    }

    public String getRole() {
      return role;
    }

    public Builder setRole(String role) {
      this.role = role;
      return this;
    }

  }

}
