package org.odata4j.edm;

import java.util.List;

import org.odata4j.core.ImmutableList;

/**
 * A CSDL ComplexType element.
 *
 * <p>A ComplexType element defines a data structure composed of {@link EdmSimpleType} properties or other complex types.
 * A complex type can be a property of an entity type or another complex type. A complex type is similar to an entity type
 * in that a complex type defines data. However, there are some key differences between complex types and entity types:
 *
 * <li>Complex types do not have identities (or keys) and therefore cannot exist independently. Complex types can only exist
 * as properties of entity types or other complex types.
 * <li>Complex types cannot participate in associations. Neither end of an association can be a complex type, and therefore
 * navigation properties cannot be defined for complex types.
 * <li>A complex type property cannot have a null value, though the scalar properties of a complex type may each be set to null.
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/cc716799.aspx">[msdn] ComplexType Element (CSDL)</a>
 */
public class EdmComplexType extends EdmStructuralType {

  private EdmComplexType(String namespace, String name, List<EdmProperty.Builder> properties,
      EdmDocumentation documentation, ImmutableList<EdmAnnotation<?>> annots,
      Boolean isAbstract) {
    super(null, namespace, name, properties, documentation, annots, isAbstract);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(EdmComplexType complexType, BuilderContext context) {
    return context.newBuilder(complexType, new Builder());
  }

  /** Mutable builder for {@link EdmComplexType} objects. */
  public static class Builder extends EdmStructuralType.Builder<EdmComplexType, Builder> {

    @Override
    Builder newBuilder(EdmComplexType complexType, BuilderContext context) {
      fillBuilder(complexType, context);
      return this;
    }

    @Override
    public EdmComplexType build() {
      return (EdmComplexType) _build();
    }

    @Override
    protected EdmType buildImpl() {
      return new EdmComplexType(namespace, name, properties, getDocumentation(), ImmutableList.copyOf(getAnnotations()), isAbstract);
    }

  }

}
