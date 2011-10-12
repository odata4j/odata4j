package org.odata4j.edm;

/**
 * Describes a homogeneous collection of instances of a specific type.
 */
public class EdmCollectionType extends EdmNonSimpleType {

  private final EdmType collectionType;

  public EdmCollectionType(String fullyQualifiedTypeName, EdmType collectionType) {
    super(fullyQualifiedTypeName);
    if (collectionType == null) throw new IllegalArgumentException("collectionType cannot be null");
    this.collectionType = collectionType;
  }

  public EdmType getCollectionType() {
    return collectionType;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder extends EdmType.Builder<EdmCollectionType, Builder> {
    
    private String fullyQualifiedTypeName;
    private EdmType.Builder<?, ?> collectionType;

    @Override
    Builder newBuilder(EdmCollectionType type, BuilderContext context) {
      this.fullyQualifiedTypeName = type.getFullyQualifiedTypeName();
      return this;
    }
    
    public Builder setFullyQualifiedTypeName(String fullyQualifiedTypeName) {
      this.fullyQualifiedTypeName = fullyQualifiedTypeName;
      return this;
    }

    public Builder setCollectionType(EdmType.Builder<?, ?> collectionType) {
      this.collectionType = collectionType;
      return this;
    }

    @Override
    public EdmType build() {
      return new EdmCollectionType(fullyQualifiedTypeName, collectionType.build());
    }

  }

}
