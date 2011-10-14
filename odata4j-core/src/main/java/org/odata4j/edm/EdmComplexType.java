package org.odata4j.edm;

import java.util.List;

import org.odata4j.core.ImmutableList;

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
